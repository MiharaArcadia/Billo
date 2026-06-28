package com.mihara.billio.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.prefs.SettingsRepository
import com.mihara.billio.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val name: String = "",
    val revenue: Double = 0.0,
    val goal: Double = 0.0,
    val openSum: Double = 0.0,
    val openCount: Int = 0,
    val overdueSum: Double = 0.0,
    val overdueCount: Int = 0,
    val recent: List<InvoiceFull> = emptyList()
) {
    val revenueProgress: Float get() = if (goal <= 0) 0f else (revenue / goal).toFloat()
    val openProgress: Float get() = if (openSum + revenue <= 0) 0f else (openSum / (openSum + revenue)).toFloat()
    val overdueProgress: Float get() = if (openSum <= 0) 0f else (overdueSum / openSum).toFloat()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    init {
        viewModelScope.launch { invoiceRepository.refreshOverdue() }
    }

    private val metrics = combine(
        invoiceRepository.observePaidThisMonth(),
        invoiceRepository.observeOpenGross(),
        invoiceRepository.observeOpenCount(),
        invoiceRepository.observeOverdueGross(),
        invoiceRepository.observeOverdueCount()
    ) { revenue, openSum, openCount, overdueSum, overdueCount ->
        arrayOf<Any>(revenue, openSum, openCount, overdueSum, overdueCount)
    }

    val state: StateFlow<DashboardState> = combine(
        metrics,
        settingsRepository.settings,
        invoiceRepository.observeRecent(5)
    ) { m, settings, recent ->
        DashboardState(
            name = settings.name,
            revenue = m[0] as Double,
            goal = settings.monthlyGoal,
            openSum = m[1] as Double,
            openCount = m[2] as Int,
            overdueSum = m[3] as Double,
            overdueCount = m[4] as Int,
            recent = recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    fun setGoal(goal: Double) {
        viewModelScope.launch { settingsRepository.setMonthlyGoal(goal) }
    }
}
