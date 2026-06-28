package com.mihara.billio.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.R
import com.mihara.billio.ui.components.BillioTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen(
    onDone: () -> Unit,
    viewModel: ClientFormViewModel = hiltViewModel()
) {
    val client by viewModel.client.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.client_detail)) },
                navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.Default.Close, null) } },
                actions = {
                    TextButton(enabled = viewModel.isValid, onClick = { viewModel.save(onDone) }) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BillioTextField(client.name, { v -> viewModel.update { it.copy(name = v) } }, stringResource(R.string.field_name), Modifier.fillMaxWidth()) }
            item { BillioTextField(client.company.orEmpty(), { v -> viewModel.update { it.copy(company = v.ifBlank { null }) } }, stringResource(R.string.field_company), Modifier.fillMaxWidth()) }
            item { BillioTextField(client.street, { v -> viewModel.update { it.copy(street = v) } }, stringResource(R.string.field_street), Modifier.fillMaxWidth()) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BillioTextField(client.zip, { v -> viewModel.update { it.copy(zip = v) } }, stringResource(R.string.field_zip), Modifier.weight(1f))
                    BillioTextField(client.city, { v -> viewModel.update { it.copy(city = v) } }, stringResource(R.string.field_city), Modifier.weight(2f))
                }
            }
            item { BillioTextField(client.country, { v -> viewModel.update { it.copy(country = v) } }, stringResource(R.string.field_country), Modifier.fillMaxWidth()) }
            item { BillioTextField(client.vatId.orEmpty(), { v -> viewModel.update { it.copy(vatId = v.ifBlank { null }) } }, stringResource(R.string.field_vat_id), Modifier.fillMaxWidth()) }
            item { BillioTextField(client.email.orEmpty(), { v -> viewModel.update { it.copy(email = v.ifBlank { null }) } }, stringResource(R.string.field_email), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Email) }
            item { BillioTextField(client.phone.orEmpty(), { v -> viewModel.update { it.copy(phone = v.ifBlank { null }) } }, stringResource(R.string.field_phone), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Phone) }
            item { BillioTextField(client.notes.orEmpty(), { v -> viewModel.update { it.copy(notes = v.ifBlank { null }) } }, stringResource(R.string.field_notes), Modifier.fillMaxWidth(), singleLine = false) }
        }
    }
}
