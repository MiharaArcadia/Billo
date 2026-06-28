package com.mihara.billio.data.db

import androidx.room.TypeConverter
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType

class Converters {
    @TypeConverter
    fun toType(value: String): InvoiceType = InvoiceType.valueOf(value)

    @TypeConverter
    fun fromType(value: InvoiceType): String = value.name

    @TypeConverter
    fun toStatus(value: String): InvoiceStatus = InvoiceStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(value: InvoiceStatus): String = value.name
}
