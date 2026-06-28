package com.mihara.billio.ui.onboarding

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mihara.billio.R
import com.mihara.billio.data.model.TaxMode
import com.mihara.billio.ui.components.BillioTextField
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val pager = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val logoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.update { it.copy(logoUri = uri.toString()) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    0 -> {
                        Spacer(Modifier.height(40.dp))
                        Text("📄", style = MaterialTheme.typography.headlineLarge)
                        Text(stringResource(R.string.onboarding_welcome_title), style = MaterialTheme.typography.headlineMedium)
                        Text(
                            stringResource(R.string.onboarding_welcome_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    1 -> {
                        Text(stringResource(R.string.onboarding_business_title), style = MaterialTheme.typography.headlineMedium)
                        LogoRow(draft.logoUri, onPick = { logoPicker.launch(arrayOf("image/*")) }, onRemove = { viewModel.update { it.copy(logoUri = null) } })
                        BillioTextField(draft.name, { v -> viewModel.update { it.copy(name = v) } }, stringResource(R.string.field_name), Modifier.fillMaxWidth())
                        BillioTextField(draft.street, { v -> viewModel.update { it.copy(street = v) } }, stringResource(R.string.field_street), Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BillioTextField(draft.zip, { v -> viewModel.update { it.copy(zip = v) } }, stringResource(R.string.field_zip), Modifier.weight(1f))
                            BillioTextField(draft.city, { v -> viewModel.update { it.copy(city = v) } }, stringResource(R.string.field_city), Modifier.weight(2f))
                        }
                        BillioTextField(draft.country, { v -> viewModel.update { it.copy(country = v) } }, stringResource(R.string.field_country), Modifier.fillMaxWidth())
                    }
                    2 -> {
                        Text(stringResource(R.string.onboarding_tax_title), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.tax_status), style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = draft.taxMode == TaxMode.SMALL_BUSINESS,
                                onClick = { viewModel.update { it.copy(taxMode = TaxMode.SMALL_BUSINESS) } },
                                label = { Text(stringResource(R.string.tax_small_business)) }
                            )
                            FilterChip(
                                selected = draft.taxMode == TaxMode.REGULAR,
                                onClick = { viewModel.update { it.copy(taxMode = TaxMode.REGULAR) } },
                                label = { Text(stringResource(R.string.tax_regular)) }
                            )
                        }
                        BillioTextField(
                            draft.vatId, { v -> viewModel.update { it.copy(vatId = v) } },
                            stringResource(R.string.field_vat_id), Modifier.fillMaxWidth()
                        )
                        Text(stringResource(R.string.default_vat_rate), style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.0, 0.07, 0.19).forEach { rate ->
                                FilterChip(
                                    selected = draft.defaultVatRate == rate,
                                    onClick = { viewModel.update { it.copy(defaultVatRate = rate) } },
                                    label = { Text("${(rate * 100).toInt()}%") }
                                )
                            }
                        }
                        BillioTextField(
                            draft.defaultPaymentTermDays.toString(),
                            { v -> viewModel.update { it.copy(defaultPaymentTermDays = v.toIntOrNull() ?: 0) } },
                            stringResource(R.string.default_payment_term), Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Number
                        )
                    }
                    3 -> {
                        Text(stringResource(R.string.onboarding_bank_title), style = MaterialTheme.typography.headlineMedium)
                        BillioTextField(draft.iban, { v -> viewModel.update { it.copy(iban = v) } }, stringResource(R.string.field_iban), Modifier.fillMaxWidth())
                        BillioTextField(draft.bic, { v -> viewModel.update { it.copy(bic = v) } }, stringResource(R.string.field_bic), Modifier.fillMaxWidth())
                        BillioTextField(draft.email, { v -> viewModel.update { it.copy(email = v) } }, stringResource(R.string.field_email), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Email)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pager.currentPage > 0) {
                TextButton(onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }) {
                    Text(stringResource(R.string.back))
                }
            } else Spacer(Modifier.size(1.dp))

            if (pager.currentPage < 3) {
                Button(onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) {
                    Text(stringResource(R.string.next))
                }
            } else {
                Button(enabled = viewModel.isValid, onClick = { viewModel.finish(onDone) }) {
                    Text(stringResource(R.string.onboarding_finish))
                }
            }
        }
    }
}

@Composable
private fun LogoRow(logoUri: String?, onPick: () -> Unit, onRemove: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(64.dp)
        ) {
            if (logoUri != null) {
                AsyncImage(
                    model = logoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape)
                )
            } else {
                Box(contentAlignment = Alignment.Center) { Text("🖼️") }
            }
        }
        OutlinedButton(onClick = onPick) { Text(stringResource(R.string.pick_logo)) }
        if (logoUri != null) {
            TextButton(onClick = onRemove) { Text(stringResource(R.string.remove_logo), color = Color(0xFFFF5252)) }
        }
    }
}
