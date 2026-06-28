package com.mihara.billio.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository
) : ViewModel() {

    private val type = MutableStateFlow(InvoiceType.INVOICE)

    fun bind(t: InvoiceType) { type.value = t }

    val items: StateFlow<List<InvoiceFull>> = type
        .flatMapLatest { repository.observeByType(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markPaid(id: Long) = viewModelScope.launch { repository.markPaid(id) }
    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }
    fun convert(id: Long) = viewModelScope.launch { repository.convertToInvoice(id) }
}
