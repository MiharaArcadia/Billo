package com.mihara.billio.util

import com.mihara.billio.data.db.entity.InvoiceItem
import kotlin.math.roundToLong

data class VatGroup(val rate: Double, val net: Double, val vat: Double)

data class Totals(
    val net: Double,
    val vat: Double,
    val gross: Double,
    val vatGroups: List<VatGroup>
)

object InvoiceCalc {

    fun lineNet(item: InvoiceItem): Double = round2(item.quantity * item.unitPrice)

    /**
     * Sums a document. On small-business documents VAT is forced to 0 regardless
     * of per-item rates, per §19 UStG.
     */
    fun totals(items: List<InvoiceItem>, smallBusiness: Boolean): Totals {
        val net = round2(items.sumOf { it.quantity * it.unitPrice })
        if (smallBusiness) {
            return Totals(net = net, vat = 0.0, gross = net, vatGroups = emptyList())
        }
        val groups = items
            .groupBy { it.vatRate }
            .map { (rate, group) ->
                val groupNet = round2(group.sumOf { it.quantity * it.unitPrice })
                VatGroup(rate = rate, net = groupNet, vat = round2(groupNet * rate))
            }
            .filter { it.rate > 0.0 || it.net != 0.0 }
            .sortedBy { it.rate }
        val vat = round2(groups.sumOf { it.vat })
        return Totals(net = net, vat = vat, gross = round2(net + vat), vatGroups = groups)
    }

    fun round2(value: Double): Double = (value * 100.0).roundToLong() / 100.0
}
