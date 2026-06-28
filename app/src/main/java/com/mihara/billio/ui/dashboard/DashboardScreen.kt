package com.mihara.billio.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.R
import com.mihara.billio.ui.components.EmptyState
import com.mihara.billio.ui.components.InvoiceCard
import com.mihara.billio.ui.components.RingChart
import com.mihara.billio.ui.theme.Accent
import com.mihara.billio.ui.theme.Danger
import com.mihara.billio.ui.theme.Info
import com.mihara.billio.util.Money

@Composable
fun DashboardScreen(
    onNewInvoice: () -> Unit,
    onNewQuote: () -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onSeeAll: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                stringResource(R.string.dashboard_greeting, state.name.ifBlank { "👋" }),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RingChart(
                    progress = state.revenueProgress,
                    color = Accent,
                    label = stringResource(R.string.ring_revenue),
                    centerValue = Money.format(state.revenue),
                    centerCaption = stringResource(R.string.dashboard_revenue_goal, Money.format(state.goal)),
                    modifier = Modifier.clickable { showGoalDialog = true }
                )
                RingChart(
                    progress = state.openProgress,
                    color = Info,
                    label = stringResource(R.string.ring_open),
                    centerValue = state.openCount.toString(),
                    centerCaption = Money.format(state.openSum)
                )
                RingChart(
                    progress = state.overdueProgress,
                    color = Danger,
                    label = stringResource(R.string.ring_overdue),
                    centerValue = state.overdueCount.toString(),
                    centerCaption = Money.format(state.overdueSum)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onNewInvoice, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.new_invoice))
                }
                FilledTonalButton(onClick = onNewQuote, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.RequestQuote, null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.new_quote))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.recent_activity), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onSeeAll) { Text(stringResource(R.string.tab_all)) }
            }
        }

        if (state.recent.isEmpty()) {
            item {
                EmptyState(
                    emoji = "🧾",
                    title = stringResource(R.string.empty_activity),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(state.recent, key = { it.invoice.id }) { full ->
                InvoiceCard(full = full, onClick = { onOpenInvoice(full.invoice.id) })
            }
        }

        item { Row(Modifier.padding(8.dp)) {} }
    }

    if (showGoalDialog) {
        var goalText by remember { mutableStateOf(state.goal.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text(stringResource(R.string.set_goal)) },
            text = {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setGoal(goalText.toDoubleOrNull() ?: state.goal)
                    showGoalDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
