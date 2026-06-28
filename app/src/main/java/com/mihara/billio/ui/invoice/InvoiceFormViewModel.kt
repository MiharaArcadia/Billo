package com.mihara.billio.ui.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.data.db.entity.Invoice
import com.mihara.billio.data.db.entity.InvoiceItem
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.data.prefs.SettingsRepository
import com.mihara.billio.data.repository.ClientRepository
import com.mihara.billio.data.repository.InvoiceRepository
import com.mihara.billio.util.Dates
import com.mihara.billio.util.InvoiceCalc
import com.mihara.billio.util.Totals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ItemDraft(
    val key: String = UUID.randomUUID().toString(),
    val description: String = "",
    val quantity: String = "1",
    val unit: String = "h",
    val unitPrice: String = "",
    val vatRate: Double = 0.19
) {
    fun toEntity(): InvoiceItem = InvoiceItem(
        invoiceId = 0,
        description = description.trim(),
        quantity = quantity.replace(',', '.').toDoubleOrNull() ?: 0.0,
        unit = unit,
        unitPrice = unitPrice.replace(',', '.').toDoubleOrNull() ?: 0.0,
        vatRate = vatRate,
        position = 0
    )
}

data class FormState(
    val type: InvoiceType = InvoiceType.INVOICE,
    val id: Long = 0,
    val number: String = "",
    val clientId: Long? = null,
    val issueDate: Long = System.currentTimeMillis(),
    val serviceDate: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val items: List<ItemDraft> = listOf(ItemDraft()),
    val isSmallBusiness: Boolean = true,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val loaded: Boolean = false
) {
    val isEditing: Boolean get() = id != 0L
    val isValid: Boolean
        get() = clientId != null && items.any {
            it.description.isNotBlank() && (it.unitPrice.replace(',', '.').toDoubleOrNull() ?: 0.0) != 0.0
        }
}

@HiltViewModel
class InvoiceFormViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val clientRepository: ClientRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val argId: Long = savedStateHandle.get<Long>("id") ?: 0L
    private val argType: InvoiceType =
        runCatching { InvoiceType.valueOf(savedStateHandle.get<String>("type") ?: "INVOICE") }
            .getOrDefault(InvoiceType.INVOICE)

    private val _state = MutableStateFlow(FormState(type = argType, id = argId))
    val state: StateFlow<FormState> = _state

    val clients: StateFlow<List<Client>> = clientRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            if (argId != 0L) {
                val full = invoiceRepository.getFull(argId)
                if (full != null) {
                    _state.value = FormState(
                        type = full.invoice.type,
                        id = full.invoice.id,
                        number = full.invoice.number,
                        clientId = full.invoice.clientId,
                        issueDate = full.invoice.issueDate,
                        serviceDate = full.invoice.serviceDate.orEmpty(),
                        dueDate = full.invoice.dueDate,
                        notes = full.invoice.notes.orEmpty(),
                        items = full.items.sortedBy { it.position }.map {
                            ItemDraft(
                                description = it.description,
                                quantity = it.quantity.toString(),
                                unit = it.unit,
                                unitPrice = it.unitPrice.toString(),
                                vatRate = it.vatRate
                            )
                        }.ifEmpty { listOf(ItemDraft()) },
                        isSmallBusiness = full.invoice.isSmallBusiness,
                        status = full.invoice.status,
                        loaded = true
                    )
                }
            } else {
                val now = System.currentTimeMillis()
                _state.value = _state.value.copy(
                    issueDate = now,
                    dueDate = Dates.plusDays(now, settings.defaultPaymentTermDays),
                    isSmallBusiness = settings.isSmallBusiness,
                    items = listOf(ItemDraft(vatRate = settings.defaultVatRate)),
                    loaded = true
                )
            }
        }
    }

    val totals: StateFlow<Totals> = _state.map { s ->
        InvoiceCalc.totals(s.items.map { it.toEntity() }, s.isSmallBusiness)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Totals(0.0, 0.0, 0.0, emptyList()))

    fun update(transform: (FormState) -> FormState) { _state.value = transform(_state.value) }

    fun updateItem(key: String, transform: (ItemDraft) -> ItemDraft) {
        _state.value = _state.value.copy(
            items = _state.value.items.map { if (it.key == key) transform(it) else it }
        )
    }

    fun addItem() {
        val rate = _state.value.items.lastOrNull()?.vatRate ?: 0.19
        _state.value = _state.value.copy(items = _state.value.items + ItemDraft(vatRate = rate))
    }

    fun removeItem(key: String) {
        val remaining = _state.value.items.filterNot { it.key == key }
        _state.value = _state.value.copy(items = remaining.ifEmpty { listOf(ItemDraft()) })
    }

    fun save(onSaved: () -> Unit) {
        val s = _state.value
        val clientId = s.clientId ?: return
        viewModelScope.launch {
            val items = s.items
                .filter { it.description.isNotBlank() }
                .mapIndexed { index, d -> d.toEntity().copy(position = index) }
            if (s.isEditing) {
                val original = invoiceRepository.getFull(s.id)?.invoice ?: return@launch
                invoiceRepository.save(
                    original.copy(
                        clientId = clientId,
                        number = s.number.ifBlank { original.number },
                        issueDate = s.issueDate,
                        serviceDate = s.serviceDate.ifBlank { null },
                        dueDate = s.dueDate,
                        notes = s.notes.ifBlank { null }
                    ),
                    items
                )
            } else {
                invoiceRepository.create(
                    type = s.type,
                    clientId = clientId,
                    issueDate = s.issueDate,
                    serviceDate = s.serviceDate.ifBlank { null },
                    dueDate = s.dueDate,
                    notes = s.notes.ifBlank { null },
                    status = InvoiceStatus.DRAFT,
                    items = items,
                    number = s.number
                )
            }
            onSaved()
        }
    }
}
