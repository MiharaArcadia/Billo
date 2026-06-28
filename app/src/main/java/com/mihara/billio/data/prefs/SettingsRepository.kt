package com.mihara.billio.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mihara.billio.data.model.TaxMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "billio_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val onboarding = booleanPreferencesKey("onboarding_complete")
        val name = stringPreferencesKey("name")
        val street = stringPreferencesKey("street")
        val zip = stringPreferencesKey("zip")
        val city = stringPreferencesKey("city")
        val country = stringPreferencesKey("country")
        val vatId = stringPreferencesKey("vat_id")
        val email = stringPreferencesKey("email")
        val phone = stringPreferencesKey("phone")
        val iban = stringPreferencesKey("iban")
        val bic = stringPreferencesKey("bic")
        val taxMode = stringPreferencesKey("tax_mode")
        val vatRate = doublePreferencesKey("default_vat_rate")
        val paymentTerm = intPreferencesKey("default_payment_term")
        val invoicePrefix = stringPreferencesKey("invoice_prefix")
        val quotePrefix = stringPreferencesKey("quote_prefix")
        val startNumber = intPreferencesKey("invoice_start_number")
        val logoUri = stringPreferencesKey("logo_uri")
        val goal = doublePreferencesKey("monthly_goal")
        val language = stringPreferencesKey("language_tag")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        val defaults = UserSettings()
        UserSettings(
            onboardingComplete = p[Keys.onboarding] ?: defaults.onboardingComplete,
            name = p[Keys.name] ?: defaults.name,
            street = p[Keys.street] ?: defaults.street,
            zip = p[Keys.zip] ?: defaults.zip,
            city = p[Keys.city] ?: defaults.city,
            country = p[Keys.country] ?: defaults.country,
            vatId = p[Keys.vatId] ?: defaults.vatId,
            email = p[Keys.email] ?: defaults.email,
            phone = p[Keys.phone] ?: defaults.phone,
            iban = p[Keys.iban] ?: defaults.iban,
            bic = p[Keys.bic] ?: defaults.bic,
            taxMode = p[Keys.taxMode]?.let { runCatching { TaxMode.valueOf(it) }.getOrNull() } ?: defaults.taxMode,
            defaultVatRate = p[Keys.vatRate] ?: defaults.defaultVatRate,
            defaultPaymentTermDays = p[Keys.paymentTerm] ?: defaults.defaultPaymentTermDays,
            invoicePrefix = p[Keys.invoicePrefix] ?: defaults.invoicePrefix,
            quotePrefix = p[Keys.quotePrefix] ?: defaults.quotePrefix,
            invoiceStartNumber = p[Keys.startNumber] ?: defaults.invoiceStartNumber,
            logoUri = p[Keys.logoUri] ?: defaults.logoUri,
            monthlyGoal = p[Keys.goal] ?: defaults.monthlyGoal,
            languageTag = p[Keys.language] ?: defaults.languageTag
        )
    }

    suspend fun save(s: UserSettings) {
        context.dataStore.edit { p ->
            p[Keys.onboarding] = s.onboardingComplete
            p[Keys.name] = s.name
            p[Keys.street] = s.street
            p[Keys.zip] = s.zip
            p[Keys.city] = s.city
            p[Keys.country] = s.country
            p[Keys.vatId] = s.vatId
            p[Keys.email] = s.email
            p[Keys.phone] = s.phone
            p[Keys.iban] = s.iban
            p[Keys.bic] = s.bic
            p[Keys.taxMode] = s.taxMode.name
            p[Keys.vatRate] = s.defaultVatRate
            p[Keys.paymentTerm] = s.defaultPaymentTermDays
            p[Keys.invoicePrefix] = s.invoicePrefix
            p[Keys.quotePrefix] = s.quotePrefix
            p[Keys.startNumber] = s.invoiceStartNumber
            if (s.logoUri != null) p[Keys.logoUri] = s.logoUri else p.remove(Keys.logoUri)
            p[Keys.goal] = s.monthlyGoal
            p[Keys.language] = s.languageTag
        }
    }

    suspend fun setMonthlyGoal(goal: Double) {
        context.dataStore.edit { it[Keys.goal] = goal }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[Keys.onboarding] = true }
    }
}
