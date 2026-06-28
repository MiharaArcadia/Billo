package com.mihara.billio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.util.Dates
import com.mihara.billio.util.InvoiceCalc
import com.mihara.billio.util.Money

@Composable
fun InvoiceCard(
    full: InvoiceFull,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val inv = full.invoice
    val totals = InvoiceCalc.totals(full.items, inv.isSmallBusiness)
    val visual = statusVisual(inv.status)
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    full.client.company?.takeIf { it.isNotBlank() } ?: full.client.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${inv.number} · ${Dates.format(inv.issueDate)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(Money.format(totals.gross), style = MaterialTheme.typography.titleMedium)
                StatusChip(visual.label, visual.color)
            }
        }
    }
}
