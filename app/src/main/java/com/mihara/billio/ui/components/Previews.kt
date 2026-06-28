package com.mihara.billio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mihara.billio.ui.theme.Accent
import com.mihara.billio.ui.theme.BillioTheme
import com.mihara.billio.ui.theme.Danger
import com.mihara.billio.ui.theme.Info
import com.mihara.billio.ui.theme.Success

@Preview(name = "Rings · Light", showBackground = true)
@Preview(name = "Rings · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun RingsPreview() {
    BillioTheme {
        Surface {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RingChart(0.7f, Accent, "Einnahmen", "2.100 €", "Ziel: 3.000 €")
                RingChart(0.4f, Info, "Offen", "3", "1.200 €")
                RingChart(0.2f, Danger, "Überfällig", "1", "400 €")
            }
        }
    }
}

@Preview(name = "Chips · Light", showBackground = true)
@Preview(name = "Chips · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ChipsPreview() {
    BillioTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("Bezahlt", Success)
                StatusChip("Überfällig", Danger)
                StatusChip("Entwurf", Info)
            }
        }
    }
}

@Preview(name = "Empty · Light", showBackground = true)
@Preview(name = "Empty · Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EmptyPreview() {
    BillioTheme {
        Surface { EmptyState("🧾", "Noch keine Rechnungen", subtitle = "Erstelle deine erste Rechnung.") }
    }
}
