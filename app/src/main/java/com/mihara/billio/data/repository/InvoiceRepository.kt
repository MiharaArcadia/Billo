package com.mihara.billio.data.repository

import com.mihara.billio.data.db.dao.CounterDao
import com.mihara.billio.data.db.dao.InvoiceDao
import com.mihara.billio.data.db.entity.Invoice
import com.mihara.billio.data.db.entity.InvoiceItem
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.data.prefs.SettingsRepository
import com.mihara.billio.data.prefs.UserSettings
import com.mihara.billio.util.Dates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val counterDao: CounterDao,
    private val settingsRepository: SettingsRepository
) {
    fun observeByType(type: InvoiceType): Flow<List<InvoiceFull>> = invoiceDao.observeByType(type)
    fun observeRecent(limit: Int = 5): Flow<List<InvoiceFull>> = invoiceDao.observeRecent(limit)
    fun observeFull(id: Long): Flow<InvoiceFull?> = invoiceDao.observeFull(id)
    fun observeForClient(clientId: Long) = invoiceDao.observeForClient(clientId)
    fun observeFullForClient(clientId: Long) = invoiceDao.observeFullForClient(clientId)
    suspend fun getFull(id: Long): InvoiceFull? = invoiceDao.getFull(id)
    suspend fun getAllFull(): List<InvoiceFull> = invoiceDao.getAllFull()

    // Dashboard
    fun observePaidThisMonth(): Flow<Double> =
        invoiceDao.observePaidGrossBetween(Dates.startOfMonth(), Dates.endOfMonth())
    fun observeOpenGross(): Flow<Double> = invoiceDao.observeOpenGross()
    fun observeOpenCount(): Flow<Int> = invoiceDao.observeOpenCount()
    fun observeOverdueGross(): Flow<Double> = invoiceDao.observeOverdueGross()
    fun observeOverdueCount(): Flow<Int> = invoiceDao.observeOverdueCount()

    suspend fun refreshOverdue() = invoiceDao.refreshOverdue(System.currentTimeMillis())

    /** Generates the next gap-free number for a document type at the given issue date. */
    suspend fun nextNumber(type: InvoiceType, issueDate: Long, settings: UserSettings): String {
        val year = Dates.yearOf(issueDate)
        val prefix = if (type == InvoiceType.QUOTE) settings.quotePrefix else settings.invoicePrefix
        val key = "$type-$year"
        val start = if (type == InvoiceType.QUOTE) 1 else settings.invoiceStartNumber
        val n = counterDao.next(key, start)
        return "%s-%d-%03d".format(prefix, year, n)
    }

    /**
     * Creates a new document. If [number] is blank a fresh sequential number is reserved.
     * The §19 flag is snapshotted from settings.
     */
    suspend fun create(
        type: InvoiceType,
        clientId: Long,
        issueDate: Long,
        serviceDate: String?,
        dueDate: Long,
        notes: String?,
        status: InvoiceStatus,
        items: List<InvoiceItem>,
        number: String? = null
    ): Long {
        val settings = settingsRepository.settings.first()
        val resolvedNumber = number?.takeIf { it.isNotBlank() }
            ?: nextNumber(type, issueDate, settings)
        val invoice = Invoice(
            number = resolvedNumber,
            type = type,
            status = status,
            clientId = clientId,
            issueDate = issueDate,
            serviceDate = serviceDate,
            dueDate = dueDate,
            notes = notes,
            isSmallBusiness = settings.isSmallBusiness
        )
        return invoiceDao.insertWithItems(invoice, items)
    }

    suspend fun save(invoice: Invoice, items: List<InvoiceItem>) =
        invoiceDao.replaceItems(invoice, items)

    suspend fun markPaid(id: Long) = invoiceDao.markPaid(id, System.currentTimeMillis())
    suspend fun setStatus(id: Long, status: InvoiceStatus) = invoiceDao.setStatus(id, status)
    suspend fun delete(id: Long) = invoiceDao.deleteInvoice(id)

    /** Quote -> invoice. Assigns a new invoice number and marks the quote ACCEPTED. */
    suspend fun convertToInvoice(quoteId: Long): Long? {
        val quote = invoiceDao.getFull(quoteId) ?: return null
        val now = System.currentTimeMillis()
        val settings = settingsRepository.settings.first()
        val dueDate = Dates.plusDays(now, settings.defaultPaymentTermDays)
        val newId = create(
            type = InvoiceType.INVOICE,
            clientId = quote.invoice.clientId,
            issueDate = now,
            serviceDate = quote.invoice.serviceDate,
            dueDate = dueDate,
            notes = quote.invoice.notes,
            status = InvoiceStatus.DRAFT,
            items = quote.items.map { it.copy(id = 0, invoiceId = 0) }
        )
        invoiceDao.setStatus(quoteId, InvoiceStatus.ACCEPTED)
        return newId
    }

    /**
     * EU-compliant cancellation: never deletes the original. Creates a credit note with
     * negated amounts and a fresh number, then flags the original CANCELLED.
     */
    suspend fun cancelInvoice(invoiceId: Long): Long? {
        val original = invoiceDao.getFull(invoiceId) ?: return null
        val now = System.currentTimeMillis()
        val settings = settingsRepository.settings.first()
        val creditNumber = nextNumber(InvoiceType.CREDIT_NOTE, now, settings)
        val credit = Invoice(
            number = creditNumber,
            type = InvoiceType.CREDIT_NOTE,
            status = InvoiceStatus.SENT,
            clientId = original.invoice.clientId,
            issueDate = now,
            serviceDate = original.invoice.serviceDate,
            dueDate = now,
            notes = "Storno zu ${original.invoice.number}",
            isSmallBusiness = original.invoice.isSmallBusiness
        )
        val creditItems = original.items.map {
            it.copy(id = 0, invoiceId = 0, quantity = -it.quantity)
        }
        val creditId = invoiceDao.insertWithItems(credit, creditItems)
        invoiceDao.update(
            original.invoice.copy(
                status = InvoiceStatus.CANCELLED,
                cancelledByNumber = creditNumber
            )
        )
        return creditId
    }
}
