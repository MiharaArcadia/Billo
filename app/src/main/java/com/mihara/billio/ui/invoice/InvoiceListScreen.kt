package com.mihara.billio.ui.invoice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.R
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.ui.components.ConfirmDialog
import com.mihara.billio.ui.components.EmptyState
import com.mihara.billio.ui.components.InvoiceCard
import com.mihara.billio.ui.components.SwipeAction
import com.mihara.billio.ui.components.SwipeableCard
import com.mihara.billio.ui.theme.Info
import com.mihara.billio.ui.theme.Success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    type: InvoiceType,
    onOpen: (Long) -> Unit,
    onCreate: () -> Unit,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    LaunchedEffect(type) { viewModel.bind(type) }
    val all by viewModel.items.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    val isQuote = type == InvoiceType.QUOTE
    val tabs = if (isQuote) {
        listOf(R.string.tab_all, R.string.status_draft, R.string.status_sent, R.string.status_accepted)
    } else {
        listOf(R.string.tab_all, R.string.tab_open, R.string.tab_overdue, R.string.tab_paid)
    }

    val filtered = remember(all, tab, isQuote) {
        when {
            tab == 0 -> all
            isQuote -> when (tab) {
                1 -> all.filter { it.invoice.status == InvoiceStatus.DRAFT }
                2 -> all.filter { it.invoice.status == InvoiceStatus.SENT }
                else -> all.filter { it.invoice.status == InvoiceStatus.ACCEPTED }
            }
            else -> when (tab) {
                1 -> all.filter { it.invoice.status in setOf(InvoiceStatus.SENT, InvoiceStatus.OVERDUE) }
                2 -> all.filter { it.invoice.status == InvoiceStatus.OVERDUE }
                else -> all.filter { it.invoice.status == InvoiceStatus.PAID }
            }
        }
    }

    var pendingPaid by remember { mutableStateOf<Long?>(null) }
    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    var pendingConvert by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(if (isQuote) R.string.nav_quotes else R.string.nav_invoices))
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            androidx.compose.foundation.layout.Column {
                ScrollableTabRow(selectedTabIndex = tab, edgePadding = 12.dp) {
                    tabs.forEachIndexed { i, res ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(res)) })
                    }
                }
                if (filtered.isEmpty()) {
                    EmptyState(
                        emoji = if (isQuote) "📝" else "🧾",
                        title = stringResource(if (isQuote) R.string.empty_quotes else R.string.empty_invoices)
                    )
                } else {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.invoice.id }) { full ->
                            SwipeRow(full, isQuote, onOpen,
                                onPaid = { pendingPaid = full.invoice.id },
                                onDelete = { pendingDelete = full.invoice.id },
                                onConvert = { pendingConvert = full.invoice.id })
                        }
                    }
                }
            }
        }
    }

    pendingPaid?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_paid_title),
            onConfirm = { viewModel.markPaid(id); pendingPaid = null },
            onDismiss = { pendingPaid = null }
        )
    }
    pendingDelete?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_message),
            onConfirm = { viewModel.delete(id); pendingDelete = null },
            onDismiss = { pendingDelete = null }
        )
    }
    pendingConvert?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_convert_title),
            message = stringResource(R.string.confirm_convert_message),
            onConfirm = { viewModel.convert(id); pendingConvert = null },
            onDismiss = { pendingConvert = null }
        )
    }
}

@Composable
private fun SwipeRow(
    full: InvoiceFull,
    isQuote: Boolean,
    onOpen: (Long) -> Unit,
    onPaid: () -> Unit,
    onDelete: () -> Unit,
    onConvert: () -> Unit
) {
    val rightAction = if (isQuote) {
        SwipeAction(Icons.AutoMirrored.Filled.ArrowForward, Info, onConvert)
    } else {
        SwipeAction(Icons.Default.Check, Success, onPaid)
    }
    SwipeableCard(
        rightAction = rightAction,
        leftAction = SwipeAction(Icons.Default.Delete, Color(0xFFFF5252), onDelete)
    ) {
        InvoiceCard(full = full, onClick = { onOpen(full.invoice.id) })
    }
}
