package com.mihara.billio.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("clientId"), Index("number", unique = true)]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val type: InvoiceType,
    val status: InvoiceStatus,
    val clientId: Long,
    val issueDate: Long,
    val serviceDate: String? = null,
    val dueDate: Long,
    val notes: String? = null,
    /** Snapshot of §19 status at creation time so historic documents stay correct. */
    val isSmallBusiness: Boolean,
    /** Set on a cancelled invoice that has been superseded by a credit note. */
    val cancelledByNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
)
