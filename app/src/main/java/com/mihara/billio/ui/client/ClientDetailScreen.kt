package com.mihara.billio.ui.client

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.R
import com.mihara.billio.ui.components.InvoiceCard
import com.mihara.billio.ui.theme.Danger
import com.mihara.billio.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenInvoice: (Long) -> Unit,
    viewModel: ClientDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val client = state.client

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(client?.let { it.company?.takeIf { c -> c.isNotBlank() } ?: it.name } ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    client?.let { IconButton(onClick = { onEdit(it.id) }) { Icon(Icons.Default.Edit, null) } }
                }
            )
        }
    ) { padding ->
        if (client == null) {
            Column(Modifier.padding(padding).fillMaxSize()) {}
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(client.name, style = MaterialTheme.typography.titleMedium)
                        Text("${client.street}, ${client.zip} ${client.city}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        client.vatId?.takeIf { it.isNotBlank() }?.let { Text("${stringResource(R.string.field_vat_id)}: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        client.email?.takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        client.phone?.takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        label = stringResource(R.string.client_open_amount, ""),
                        value = Money.format(state.openAmount),
                        modifier = Modifier.weight(1f),
                        highlight = state.openAmount > 0
                    )
                    SummaryCard(
                        label = stringResource(R.string.client_total_billed, ""),
                        value = Money.format(state.totalBilled),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item { Text(stringResource(R.string.client_history), style = MaterialTheme.typography.titleMedium) }
            items(state.invoices, key = { it.invoice.id }) { full ->
                InvoiceCard(full = full, onClick = { onOpenInvoice(full.invoice.id) })
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label.trim().ifBlank { " " }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = if (highlight) Danger else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
