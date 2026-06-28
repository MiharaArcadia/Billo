package com.mihara.billio.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.data.db.entity.Invoice
import com.mihara.billio.data.db.entity.InvoiceItem

/** Invoice joined with its client and line items — used for detail view and PDF. */
data class InvoiceFull(
    @Embedded val invoice: Invoice,
    @Relation(parentColumn = "clientId", entityColumn = "id")
    val client: Client,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<InvoiceItem>
)
