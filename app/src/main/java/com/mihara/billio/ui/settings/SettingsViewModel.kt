package com.mihara.billio.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihara.billio.data.prefs.SettingsRepository
import com.mihara.billio.data.prefs.UserSettings
import com.mihara.billio.data.repository.InvoiceRepository
import com.mihara.billio.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val invoiceRepository: InvoiceRepository,
    private val pdfGenerator: PdfGenerator
) : ViewModel() {

    private val _draft = MutableStateFlow(UserSettings())
    val draft: StateFlow<UserSettings> = _draft

    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri: StateFlow<Uri?> = _exportUri

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    init {
        viewModelScope.launch { _draft.value = settingsRepository.settings.first() }
    }

    fun update(transform: (UserSettings) -> UserSettings) { _draft.value = transform(_draft.value) }

    fun save() {
        viewModelScope.launch {
            settingsRepository.save(_draft.value)
            applyLanguage(_draft.value.languageTag)
            _saved.value = true
        }
    }

    fun clearSaved() { _saved.value = false }

    private fun applyLanguage(tag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val lm = context.getSystemService(android.app.LocaleManager::class.java)
            lm?.applicationLocales = if (tag == "system") {
                android.os.LocaleList.getEmptyLocaleList()
            } else {
                android.os.LocaleList.forLanguageTags(tag)
            }
        }
    }

    fun export() {
        viewModelScope.launch {
            _busy.value = true
            runCatching {
                val settings = settingsRepository.settings.first()
                val locale = when (settings.languageTag) {
                    "de" -> Locale.GERMANY
                    "en" -> Locale.ENGLISH
                    else -> Locale.getDefault()
                }
                val all = invoiceRepository.getAllFull()
                withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                    val zipFile = File(dir, "billio_export.zip")
                    ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
                        all.forEach { full ->
                            val pdf = pdfGenerator.generate(full, settings, locale)
                            zip.putNextEntry(ZipEntry(pdf.name))
                            pdf.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                    pdfGenerator.shareUri(zipFile)
                }
            }.onSuccess { _exportUri.value = it }
            _busy.value = false
        }
    }

    fun clearExport() { _exportUri.value = null }
}
