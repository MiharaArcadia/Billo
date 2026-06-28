package com.mihara.billio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.prefs.SettingsRepository
import com.mihara.billio.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    invoiceRepository: InvoiceRepository
) : ViewModel() {

    sealed interface State {
        data object Loading : State
        data class Ready(val onboardingComplete: Boolean) : State
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    init {
        viewModelScope.launch {
            invoiceRepository.refreshOverdue()
            val s = settingsRepository.settings.first()
            _state.value = State.Ready(s.onboardingComplete)
        }
    }
}
