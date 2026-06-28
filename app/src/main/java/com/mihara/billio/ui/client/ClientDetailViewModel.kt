package com.mihara.billio.ui.client

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.data.repository.ClientRepository
import com.mihara.billio.data.repository.InvoiceRepository
import com.mihara.billio.util.InvoiceCalc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ClientDetailState(
    val client: Client? = null,
    val invoices: List<InvoiceFull> = emptyList(),
    val openAmount: Double = 0.0,
    val totalBilled: Double = 0.0
)

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    clientRepository: ClientRepository,
    invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id: Long = savedStateHandle.get<Long>("id") ?: 0L

    val state: StateFlow<ClientDetailState> = combine(
        clientRepository.observe(id),
        invoiceRepository.observeFullForClient(id)
    ) { client, invoices ->
        fun gross(f: InvoiceFull) = InvoiceCalc.totals(f.items, f.invoice.isSmallBusiness).gross
        val invoicesOnly = invoices.filter { it.invoice.type == InvoiceType.INVOICE }
        ClientDetailState(
            client = client,
            invoices = invoices,
            openAmount = invoicesOnly
                .filter { it.invoice.status in setOf(InvoiceStatus.SENT, InvoiceStatus.OVERDUE) }
                .sumOf { gross(it) },
            totalBilled = invoicesOnly
                .filter { it.invoice.status != InvoiceStatus.CANCELLED }
                .sumOf { gross(it) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientDetailState())
}
