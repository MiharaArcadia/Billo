package com.mihara.billio.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.mihara.billio.R
import com.mihara.billio.data.model.InvoiceStatus
import com.mihara.billio.ui.theme.Accent
import com.mihara.billio.ui.theme.Danger
import com.mihara.billio.ui.theme.Info
import com.mihara.billio.ui.theme.Success

data class StatusVisual(val label: String, val color: Color)

@Composable
fun statusVisual(status: InvoiceStatus): StatusVisual = when (status) {
    InvoiceStatus.DRAFT -> StatusVisual(stringResource(R.string.status_draft), Color(0xFF9E9E9E))
    InvoiceStatus.SENT -> StatusVisual(stringResource(R.string.status_sent), Info)
    InvoiceStatus.PAID -> StatusVisual(stringResource(R.string.status_paid), Success)
    InvoiceStatus.OVERDUE -> StatusVisual(stringResource(R.string.status_overdue), Danger)
    InvoiceStatus.CANCELLED -> StatusVisual(stringResource(R.string.status_cancelled), Color(0xFF9E9E9E))
    InvoiceStatus.ACCEPTED -> StatusVisual(stringResource(R.string.status_accepted), Success)
    InvoiceStatus.REJECTED -> StatusVisual(stringResource(R.string.status_rejected), Danger)
}
