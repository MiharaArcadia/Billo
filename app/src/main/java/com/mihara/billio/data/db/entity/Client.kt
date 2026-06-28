package com.mihara.billio.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val company: String? = null,
    val street: String = "",
    val zip: String = "",
    val city: String = "",
    val country: String = "",
    val vatId: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
