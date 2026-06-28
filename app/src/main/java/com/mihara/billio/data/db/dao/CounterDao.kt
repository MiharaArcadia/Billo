package com.mihara.billio.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mihara.billio.data.db.entity.Counter

@Dao
interface CounterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(counter: Counter)

    @Query("UPDATE counters SET value = value + 1 WHERE key = :key")
    suspend fun increment(key: String)

    @Query("SELECT value FROM counters WHERE key = :key")
    suspend fun current(key: String): Int?

    /**
     * Atomically reserves and returns the next number for [key]. Seeds the counter
     * at [start] - 1 on first use so the first issued number equals [start].
     */
    @Transaction
    suspend fun next(key: String, start: Int): Int {
        insertIfAbsent(Counter(key, start - 1))
        increment(key)
        return current(key) ?: start
    }
}
