package com.mihara.billio.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.mihara.billio.data.db.entity.Invoice
import com.mihara.billio.data.db.entity.InvoiceItem

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItem>
)
