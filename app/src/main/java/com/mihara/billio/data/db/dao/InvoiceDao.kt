package com.mihara.billio.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mihara.billio.data.db.entity.Invoice
import com.mihara.billio.data.db.entity.InvoiceItem
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.db.relation.InvoiceWithItems
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Transaction
    @Query("SELECT * FROM invoices WHERE type = :type ORDER BY createdAt DESC")
    fun observeByType(type: InvoiceType): Flow<List<InvoiceFull>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<InvoiceFull>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeFull(id: Long): Flow<InvoiceFull?>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getFull(id: Long): InvoiceFull?

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    suspend fun getAllFull(): List<InvoiceFull>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getWithItems(id: Long): InvoiceWithItems?

    @Query("SELECT * FROM invoices WHERE clientId = :clientId AND type = 'INVOICE' ORDER BY createdAt DESC")
    fun observeForClient(clientId: Long): Flow<List<Invoice>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun observeFullForClient(clientId: Long): Flow<List<InvoiceFull>>

    // --- Dashboard aggregates ---

    @Query(
        "SELECT COALESCE(SUM(i.quantity * i.unitPrice * (1 + CASE WHEN inv.isSmallBusiness THEN 0 ELSE i.vatRate END)), 0) " +
            "FROM invoices inv JOIN invoice_items i ON i.invoiceId = inv.id " +
            "WHERE inv.type = 'INVOICE' AND inv.status = 'PAID' AND inv.paidAt BETWEEN :from AND :to"
    )
    fun observePaidGrossBetween(from: Long, to: Long): Flow<Double>

    @Query(
        "SELECT COALESCE(SUM(i.quantity * i.unitPrice * (1 + CASE WHEN inv.isSmallBusiness THEN 0 ELSE i.vatRate END)), 0) " +
            "FROM invoices inv JOIN invoice_items i ON i.invoiceId = inv.id " +
            "WHERE inv.type = 'INVOICE' AND inv.status IN ('SENT','OVERDUE')"
    )
    fun observeOpenGross(): Flow<Double>

    @Query("SELECT COUNT(*) FROM invoices WHERE type = 'INVOICE' AND status IN ('SENT','OVERDUE')")
    fun observeOpenCount(): Flow<Int>

    @Query(
        "SELECT COALESCE(SUM(i.quantity * i.unitPrice * (1 + CASE WHEN inv.isSmallBusiness THEN 0 ELSE i.vatRate END)), 0) " +
            "FROM invoices inv JOIN invoice_items i ON i.invoiceId = inv.id " +
            "WHERE inv.type = 'INVOICE' AND inv.status = 'OVERDUE'"
    )
    fun observeOverdueGross(): Flow<Double>

    @Query("SELECT COUNT(*) FROM invoices WHERE type = 'INVOICE' AND status = 'OVERDUE'")
    fun observeOverdueCount(): Flow<Int>

    // --- Mutations ---

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(invoice: Invoice): Long

    @Update
    suspend fun update(invoice: Invoice)

    @Insert
    suspend fun insertItems(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItems(invoiceId: Long)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: InvoiceStatus)

    @Query("UPDATE invoices SET status = 'PAID', paidAt = :paidAt WHERE id = :id")
    suspend fun markPaid(id: Long, paidAt: Long)

    /** Flags sent invoices whose due date has passed as OVERDUE. Run on app start. */
    @Query("UPDATE invoices SET status = 'OVERDUE' WHERE type = 'INVOICE' AND status = 'SENT' AND dueDate < :now")
    suspend fun refreshOverdue(now: Long)

    @Transaction
    suspend fun insertWithItems(invoice: Invoice, items: List<InvoiceItem>): Long {
        val id = insert(invoice)
        insertItems(items.map { it.copy(invoiceId = id) })
        return id
    }

    @Transaction
    suspend fun replaceItems(invoice: Invoice, items: List<InvoiceItem>) {
        update(invoice)
        deleteItems(invoice.id)
        insertItems(items.map { it.copy(id = 0, invoiceId = invoice.id) })
    }
}
