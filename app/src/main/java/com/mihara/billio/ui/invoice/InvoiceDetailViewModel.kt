package com.mihara.billio.ui.invoice

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.prefs.SettingsRepository
import com.mihara.billio.data.repository.InvoiceRepository
import com.mihara.billio.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val repository: InvoiceRepository,
    private val settingsRepository: SettingsRepository,
    private val pdfGenerator: PdfGenerator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: Long = savedStateHandle.get<Long>("id") ?: 0L

    val invoice: StateFlow<InvoiceFull?> = repository.observeFull(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _share = MutableStateFlow<Uri?>(null)
    val shareUri: StateFlow<Uri?> = _share

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    fun markPaid() = viewModelScope.launch { repository.markPaid(id) }
    fun setStatus(status: InvoiceStatus) = viewModelScope.launch { repository.setStatus(id, status) }
    fun delete(onDeleted: () -> Unit) = viewModelScope.launch { repository.delete(id); onDeleted() }
    fun cancel() = viewModelScope.launch { repository.cancelInvoice(id) }

    fun convert(onConverted: (Long) -> Unit) = viewModelScope.launch {
        repository.convertToInvoice(id)?.let(onConverted)
    }

    fun generateAndShare() = viewModelScope.launch {
        val full = repository.getFull(id) ?: return@launch
        _busy.value = true
        runCatching {
            val settings = settingsRepository.settings.first()
            val locale = when (settings.languageTag) {
                "de" -> Locale.GERMANY
                "en" -> Locale.ENGLISH
                else -> Locale.getDefault()
            }
            val file = pdfGenerator.generate(full, settings, locale)
            pdfGenerator.shareUri(file)
        }.onSuccess { _share.value = it }
        _busy.value = false
    }

    fun clearShare() { _share.value = null }
}
