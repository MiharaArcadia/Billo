package com.mihara.billio.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.R
import com.mihara.billio.data.db.entity.Client
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.ui.components.TotalRow
import com.mihara.billio.util.Dates
import com.mihara.billio.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceFormScreen(
    onDone: () -> Unit,
    onAddClient: () -> Unit,
    viewModel: InvoiceFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()

    val isQuote = state.type == InvoiceType.QUOTE
    var datePickerFor by remember { mutableStateOf<String?>(null) } // "issue" | "due"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isQuote) R.string.form_quote_title else R.string.form_invoice_title)) },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Default.Close, null) }
                },
                actions = {
                    TextButton(enabled = state.isValid, onClick = { viewModel.save(onDone) }) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ClientSelector(
                    clients = clients,
                    selectedId = state.clientId,
                    onSelect = { viewModel.update { s -> s.copy(clientId = it) } },
                    onAddNew = onAddClient
                )
            }
            item {
                OutlinedTextField(
                    value = state.number,
                    onValueChange = { v -> viewModel.update { it.copy(number = v) } },
                    label = { Text(stringResource(if (isQuote) R.string.quote_number else R.string.invoice_number)) },
                    placeholder = { Text("auto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateField(
                        label = stringResource(R.string.issue_date),
                        value = Dates.format(state.issueDate),
                        onClick = { datePickerFor = "issue" },
                        modifier = Modifier.weight(1f)
                    )
                    if (!isQuote) {
                        DateField(
                            label = stringResource(R.string.due_date),
                            value = Dates.format(state.dueDate),
                            onClick = { datePickerFor = "due" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = state.serviceDate,
                    onValueChange = { v -> viewModel.update { it.copy(serviceDate = v) } },
                    label = { Text(stringResource(R.string.service_date)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(stringResource(R.string.positions), style = MaterialTheme.typography.titleMedium)
            }
            items(state.items, key = { it.key }) { item ->
                ItemEditor(
                    item = item,
                    smallBusiness = state.isSmallBusiness,
                    onChange = { transform -> viewModel.updateItem(item.key, transform) },
                    onRemove = { viewModel.removeItem(item.key) }
                )
            }
            item {
                OutlinedButton(onClick = { viewModel.addItem() }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.add_position))
                }
            }

            item {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                    label = { Text(stringResource(R.string.note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TotalRow(stringResource(R.string.total_net), Money.format(totals.net))
                        if (!state.isSmallBusiness) {
                            totals.vatGroups.forEach { g ->
                                TotalRow("${stringResource(R.string.total_vat)} ${(g.rate * 100).toInt()}%", Money.format(g.vat))
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        TotalRow(stringResource(R.string.total_gross), Money.format(totals.gross), bold = true)
                        if (state.isSmallBusiness) {
                            Text(
                                stringResource(R.string.small_business_note),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    datePickerFor?.let { which ->
        val initial = if (which == "issue") state.issueDate else state.dueDate
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { datePickerFor = null },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.update {
                            if (which == "issue") it.copy(issueDate = millis) else it.copy(dueDate = millis)
                        }
                    }
                    datePickerFor = null
                }) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(onClick = { datePickerFor = null }) { Text(stringResource(R.string.cancel)) }
            }
        ) { DatePicker(state = pickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientSelector(
    clients: List<Client>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddNew: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = clients.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { it.company?.takeIf { c -> c.isNotBlank() } ?: it.name } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.select_client)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            clients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.company?.takeIf { it.isNotBlank() } ?: client.name) },
                    onClick = { onSelect(client.id); expanded = false }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.new_client)) },
                leadingIcon = { Icon(Icons.Default.Add, null) },
                onClick = { expanded = false; onAddNew() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemEditor(
    item: ItemDraft,
    smallBusiness: Boolean,
    onChange: ((ItemDraft) -> ItemDraft) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.description,
                    onValueChange = { v -> onChange { it.copy(description = v) } },
                    label = { Text(stringResource(R.string.position_description)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, null) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.quantity,
                    onValueChange = { v -> onChange { it.copy(quantity = v) } },
                    label = { Text(stringResource(R.string.position_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = item.unit,
                    onValueChange = { v -> onChange { it.copy(unit = v) } },
                    label = { Text(stringResource(R.string.position_unit)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = item.unitPrice,
                    onValueChange = { v -> onChange { it.copy(unitPrice = v) } },
                    label = { Text(stringResource(R.string.position_unit_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
            }
            if (!smallBusiness) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.position_vat), style = MaterialTheme.typography.labelMedium)
                    listOf(0.0, 0.07, 0.19).forEach { rate ->
                        FilterChip(
                            selected = item.vatRate == rate,
                            onClick = { onChange { it.copy(vatRate = rate) } },
                            label = { Text("${(rate * 100).toInt()}%") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
