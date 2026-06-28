package com.mihara.billio.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mihara.billio.data.db.dao.ClientDao
import com.mihara.billio.data.db.dao.CounterDao
import com.mihara.billio.data.db.dao.InvoiceDao
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.data.db.entity.Counter
import com.mihara.billio.data.db.entity.Invoice
import com.mihara.billio.data.db.entity.InvoiceItem

@Database(
    entities = [Client::class, Invoice::class, InvoiceItem::class, Counter::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BillioDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun counterDao(): CounterDao

    companion object {
        const val NAME = "billio.db"
    }
}
