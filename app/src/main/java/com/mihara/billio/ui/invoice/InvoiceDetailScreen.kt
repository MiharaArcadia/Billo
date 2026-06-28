package com.mihara.billio.ui.invoice

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mihara.billio.R
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.ui.components.ConfirmDialog
import com.mihara.billio.ui.components.StatusChip
import com.mihara.billio.ui.components.TotalRow
import com.mihara.billio.ui.components.statusVisual
import com.mihara.billio.util.Dates
import com.mihara.billio.util.InvoiceCalc
import com.mihara.billio.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long, String) -> Unit,
    onOpenInvoice: (Long) -> Unit,
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val full by viewModel.invoice.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val shareUri by viewModel.shareUri.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(shareUri) {
        shareUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ContextCompat.startActivity(
                context,
                Intent.createChooser(intent, context.getString(R.string.share_pdf)),
                null
            )
            viewModel.clearShare()
        }
    }

    var pendingDelete by remember { mutableStateOf(false) }
    var pendingCancel by remember { mutableStateOf(false) }
    var pendingPaid by remember { mutableStateOf(false) }

    val data = full
    val inv = data?.invoice
    val isQuote = inv?.type == InvoiceType.QUOTE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(inv?.number ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (inv != null && inv.status != InvoiceStatus.CANCELLED) {
                        IconButton(onClick = { onEdit(inv.id, inv.type.name) }) { Icon(Icons.Default.Edit, null) }
                    }
                    IconButton(onClick = { pendingDelete = true }) { Icon(Icons.Default.Delete, null) }
                }
            )
        }
    ) { padding ->
        if (data == null || inv == null) {
            Column(Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {}
            return@Scaffold
        }
        val totals = InvoiceCalc.totals(data.items, inv.isSmallBusiness)
        val visual = statusVisual(inv.status)

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusChip(visual.label, visual.color)
                Text(Dates.format(inv.issueDate), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Client
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.bill_to), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    data.client.company?.takeIf { it.isNotBlank() }?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                    Text(data.client.name)
                    Text("${data.client.street}, ${data.client.zip} ${data.client.city}")
                }
            }

            // Items
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.items.sortedBy { it.position }.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(item.description, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${Money.plain(item.quantity)} ${item.unit} × ${Money.format(item.unitPrice)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(Money.format(InvoiceCalc.lineNet(item)), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    TotalRow(stringResource(R.string.total_net), Money.format(totals.net))
                    if (!inv.isSmallBusiness) {
                        totals.vatGroups.forEach { g ->
                            TotalRow("${stringResource(R.string.total_vat)} ${(g.rate * 100).toInt()}%", Money.format(g.vat))
                        }
                    }
                    TotalRow(stringResource(R.string.total_gross), Money.format(totals.gross), bold = true)
                    if (inv.isSmallBusiness) {
                        Text(
                            stringResource(R.string.small_business_note),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            inv.notes?.takeIf { it.isNotBlank() }?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            inv.paidAt?.let { Text(stringResource(R.string.paid_on, Dates.format(it))) }

            // Actions
            FilledTonalButton(
                onClick = { viewModel.generateAndShare() },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Share, null, modifier = Modifier.padding(end = 8.dp))
                }
                Text(stringResource(R.string.share_pdf))
            }

            if (isQuote) {
                if (inv.status != InvoiceStatus.ACCEPTED) {
                    OutlinedButton(onClick = { viewModel.convert(onOpenInvoice) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.convert_to_invoice))
                    }
                }
            } else {
                if (inv.status != InvoiceStatus.PAID && inv.status != InvoiceStatus.CANCELLED) {
                    OutlinedButton(onClick = { pendingPaid = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.mark_paid))
                    }
                    OutlinedButton(onClick = { pendingCancel = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.cancel_invoice))
                    }
                }
            }
        }
    }

    if (pendingDelete) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_message),
            onConfirm = { pendingDelete = false; viewModel.delete(onBack) },
            onDismiss = { pendingDelete = false }
        )
    }
    if (pendingPaid) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_paid_title),
            onConfirm = { pendingPaid = false; viewModel.markPaid() },
            onDismiss = { pendingPaid = false }
        )
    }
    if (pendingCancel) {
        ConfirmDialog(
            title = stringResource(R.string.cancel_invoice),
            onConfirm = { pendingCancel = false; viewModel.cancel() },
            onDismiss = { pendingCancel = false }
        )
    }
}
