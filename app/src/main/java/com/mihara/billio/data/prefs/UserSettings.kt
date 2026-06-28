package com.mihara.billio.data.prefs

import com.mihara.billio.data.model.TaxMode

/** Issuer profile + invoicing defaults. Persisted in DataStore. */
data class UserSettings(
    val onboardingComplete: Boolean = false,
    val name: String = "",
    val street: String = "",
    val zip: String = "",
    val city: String = "",
    val country: String = "",
    val vatId: String = "",
    val email: String = "",
    val phone: String = "",
    val iban: String = "",
    val bic: String = "",
    val taxMode: TaxMode = TaxMode.SMALL_BUSINESS,
    val defaultVatRate: Double = 0.19,
    val defaultPaymentTermDays: Int = 14,
    val invoicePrefix: String = "RE",
    val quotePrefix: String = "AN",
    val invoiceStartNumber: Int = 1,
    val logoUri: String? = null,
    val monthlyGoal: Double = 3000.0,
    /** "system", "de" or "en". */
    val languageTag: String = "system"
) {
    val isSmallBusiness: Boolean get() = taxMode == TaxMode.SMALL_BUSINESS
}
