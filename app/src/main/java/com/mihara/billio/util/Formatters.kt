package com.mihara.billio.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

object Money {
    fun format(value: Double, locale: Locale = Locale.getDefault()): String {
        val nf = NumberFormat.getCurrencyInstance(locale)
        nf.currency = Currency.getInstance("EUR")
        return nf.format(value)
    }

    fun plain(value: Double, locale: Locale = Locale.getDefault()): String {
        val nf = NumberFormat.getNumberInstance(locale)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return nf.format(value)
    }
}

object Dates {
    fun format(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
        SimpleDateFormat("dd.MM.yyyy", locale).format(Date(epochMillis))

    fun yearOf(epochMillis: Long): Int {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochMillis
        return cal.get(java.util.Calendar.YEAR)
    }

    fun plusDays(epochMillis: Long, days: Int): Long = epochMillis + days * 24L * 60 * 60 * 1000

    fun startOfMonth(now: Long = System.currentTimeMillis()): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfMonth(now: Long = System.currentTimeMillis()): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startOfMonth(now)
        cal.add(java.util.Calendar.MONTH, 1)
        return cal.timeInMillis - 1
    }
}
