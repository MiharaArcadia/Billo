package com.mihara.billio.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Atomic, gap-free running counters. One row per (type, year), e.g. key = "INVOICE-2026".
 * Incremented inside a single transaction so numbers never repeat.
 */
@Entity(tableName = "counters")
data class Counter(
    @PrimaryKey val key: String,
    val value: Int
)
