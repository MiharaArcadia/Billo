package com.mihara.billio.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.BuildConfig
import com.mihara.billio.R
import com.mihara.billio.data.model.TaxMode
import com.mihara.billio.ui.components.BillioTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val exportUri by viewModel.exportUri.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.update { it.copy(logoUri = uri.toString()) }
        }
    }

    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ContextCompat.startActivity(context, Intent.createChooser(intent, context.getString(R.string.settings_export)), null)
            viewModel.clearExport()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                actions = { TextButton(onClick = { viewModel.save() }) { Text(stringResource(R.string.save)) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Section(stringResource(R.string.settings_profile)) }
            item { BillioTextField(draft.name, { v -> viewModel.update { it.copy(name = v) } }, stringResource(R.string.field_name), Modifier.fillMaxWidth()) }
            item { BillioTextField(draft.street, { v -> viewModel.update { it.copy(street = v) } }, stringResource(R.string.field_street), Modifier.fillMaxWidth()) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BillioTextField(draft.zip, { v -> viewModel.update { it.copy(zip = v) } }, stringResource(R.string.field_zip), Modifier.weight(1f))
                    BillioTextField(draft.city, { v -> viewModel.update { it.copy(city = v) } }, stringResource(R.string.field_city), Modifier.weight(2f))
                }
            }
            item { BillioTextField(draft.country, { v -> viewModel.update { it.copy(country = v) } }, stringResource(R.string.field_country), Modifier.fillMaxWidth()) }
            item { BillioTextField(draft.vatId, { v -> viewModel.update { it.copy(vatId = v) } }, stringResource(R.string.field_vat_id), Modifier.fillMaxWidth()) }
            item { BillioTextField(draft.iban, { v -> viewModel.update { it.copy(iban = v) } }, stringResource(R.string.field_iban), Modifier.fillMaxWidth()) }
            item { BillioTextField(draft.bic, { v -> viewModel.update { it.copy(bic = v) } }, stringResource(R.string.field_bic), Modifier.fillMaxWidth()) }
            item { BillioTextField(draft.email, { v -> viewModel.update { it.copy(email = v) } }, stringResource(R.string.field_email), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Email) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { logoPicker.launch(arrayOf("image/*")) }) { Text(stringResource(R.string.pick_logo)) }
                    if (draft.logoUri != null) {
                        TextButton(onClick = { viewModel.update { it.copy(logoUri = null) } }) { Text(stringResource(R.string.remove_logo)) }
                    }
                }
            }

            item { HorizontalDivider() }
            item { Section(stringResource(R.string.settings_invoicing)) }
            item {
                Text(stringResource(R.string.tax_status), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(draft.taxMode == TaxMode.SMALL_BUSINESS, { viewModel.update { it.copy(taxMode = TaxMode.SMALL_BUSINESS) } }, { Text(stringResource(R.string.tax_small_business)) })
                    FilterChip(draft.taxMode == TaxMode.REGULAR, { viewModel.update { it.copy(taxMode = TaxMode.REGULAR) } }, { Text(stringResource(R.string.tax_regular)) })
                }
            }
            item {
                Text(stringResource(R.string.default_vat_rate), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.0, 0.07, 0.19).forEach { rate ->
                        FilterChip(draft.defaultVatRate == rate, { viewModel.update { it.copy(defaultVatRate = rate) } }, { Text("${(rate * 100).toInt()}%") })
                    }
                }
            }
            item { BillioTextField(draft.defaultPaymentTermDays.toString(), { v -> viewModel.update { it.copy(defaultPaymentTermDays = v.toIntOrNull() ?: 0) } }, stringResource(R.string.default_payment_term), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BillioTextField(draft.invoicePrefix, { v -> viewModel.update { it.copy(invoicePrefix = v) } }, stringResource(R.string.invoice_prefix), Modifier.weight(1f))
                    BillioTextField(draft.invoiceStartNumber.toString(), { v -> viewModel.update { it.copy(invoiceStartNumber = v.toIntOrNull() ?: 1) } }, stringResource(R.string.invoice_start_number), Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
            }

            item { HorizontalDivider() }
            item { Section(stringResource(R.string.settings_language)) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(draft.languageTag == "system", { viewModel.update { it.copy(languageTag = "system") } }, { Text(stringResource(R.string.lang_system)) })
                    FilterChip(draft.languageTag == "de", { viewModel.update { it.copy(languageTag = "de") } }, { Text(stringResource(R.string.lang_de)) })
                    FilterChip(draft.languageTag == "en", { viewModel.update { it.copy(languageTag = "en") } }, { Text(stringResource(R.string.lang_en)) })
                }
            }

            item { HorizontalDivider() }
            item {
                Button(onClick = { viewModel.export() }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    if (busy) CircularProgressIndicator(Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Download, null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.settings_export))
                }
            }
            item {
                Text(
                    stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
}
