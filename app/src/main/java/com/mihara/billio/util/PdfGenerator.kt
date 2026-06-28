package com.mihara.billio.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.mihara.billio.R
import com.mihara.billio.data.db.relation.InvoiceFull
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.data.prefs.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Renders an EU-compliant invoice/quote PDF using Android's native PdfDocument. */
@Singleton
class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accent = Color.parseColor("#6C63FF")
    private val ink = Color.parseColor("#1A1A1A")
    private val muted = Color.parseColor("#6B6B6B")
    private val hairline = Color.parseColor("#DDDDDD")

    // A4 at 72dpi
    private val pageW = 595
    private val pageH = 842
    private val marginL = 48f
    private val marginR = 547f

    suspend fun generate(full: InvoiceFull, settings: UserSettings, locale: Locale): File =
        withContext(Dispatchers.IO) {
            val res = localizedResources(locale)
            val doc = PdfDocument()
            val page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, 1).create())
            drawDocument(page.canvas, full, settings, res, locale)
            doc.finishPage(page)

            val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
            val file = File(dir, "${sanitize(full.invoice.number)}.pdf")
            file.outputStream().use { doc.writeTo(it) }
            doc.close()
            file
        }

    fun shareUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    private fun drawDocument(
        c: Canvas,
        full: InvoiceFull,
        settings: UserSettings,
        res: android.content.res.Resources,
        locale: Locale
    ) {
        val inv = full.invoice
        val client = full.client
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent; textSize = 26f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val h = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 10f }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; textSize = 8.5f }
        val line = Paint().apply { color = hairline; strokeWidth = 0.8f }

        var y = 60f

        // Logo
        settings.logoUri?.let { uriStr ->
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uriStr)).use { ins ->
                    val bmp = BitmapFactory.decodeStream(ins)
                    if (bmp != null) {
                        val maxW = 130f; val maxH = 60f
                        val scale = minOf(maxW / bmp.width, maxH / bmp.height)
                        val w = bmp.width * scale; val ht = bmp.height * scale
                        c.drawBitmap(
                            android.graphics.Bitmap.createScaledBitmap(bmp, w.toInt(), ht.toInt(), true),
                            marginL, y - 12, null
                        )
                    }
                }
            }
        }

        // Document title (right)
        val docTitle = when (inv.type) {
            InvoiceType.QUOTE -> res.getString(R.string.pdf_quote)
            InvoiceType.CREDIT_NOTE -> res.getString(R.string.pdf_credit_note)
            else -> res.getString(R.string.pdf_invoice)
        }
        title.textAlign = Paint.Align.RIGHT
        c.drawText(docTitle, marginR, y + 6, title)
        title.textAlign = Paint.Align.LEFT

        // Issuer line under logo area
        y = 116f
        c.drawText(settings.name, marginL, y, h)
        y += 13
        c.drawText("${settings.street}, ${settings.zip} ${settings.city}", marginL, y, small)
        if (settings.country.isNotBlank()) { y += 11; c.drawText(settings.country, marginL, y, small) }
        if (settings.vatId.isNotBlank() && !settings.isSmallBusiness) {
            y += 11; c.drawText("${res.getString(R.string.field_vat_id)}: ${settings.vatId}", marginL, y, small)
        }

        // Recipient block
        var ry = 170f
        c.drawText(res.getString(R.string.bill_to), marginL, ry, small)
        ry += 15
        client.company?.takeIf { it.isNotBlank() }?.let { c.drawText(it, marginL, ry, h); ry += 13 }
        c.drawText(client.name, marginL, ry, body); ry += 12
        c.drawText("${client.street}", marginL, ry, body); ry += 12
        c.drawText("${client.zip} ${client.city}", marginL, ry, body); ry += 12
        if (client.country.isNotBlank()) { c.drawText(client.country, marginL, ry, body); ry += 12 }
        client.vatId?.takeIf { it.isNotBlank() }?.let {
            c.drawText("${res.getString(R.string.field_vat_id)}: $it", marginL, ry, small); ry += 12
        }

        // Meta block (right)
        val mx = 360f
        var my = 170f
        val numLabel = if (inv.type == InvoiceType.QUOTE) res.getString(R.string.pdf_quote_no) else res.getString(R.string.pdf_invoice_no)
        fun meta(label: String, value: String) {
            c.drawText(label, mx, my, small)
            body.textAlign = Paint.Align.RIGHT
            c.drawText(value, marginR, my, body)
            body.textAlign = Paint.Align.LEFT
            my += 15
        }
        meta(numLabel, inv.number)
        meta(res.getString(R.string.pdf_date), Dates.format(inv.issueDate, locale))
        inv.serviceDate?.takeIf { it.isNotBlank() }?.let { meta(res.getString(R.string.pdf_service_period), it) }
        if (inv.type == InvoiceType.INVOICE) meta(res.getString(R.string.pdf_due), Dates.format(inv.dueDate, locale))

        // Items table
        var ty = maxOf(ry, my) + 24
        c.drawLine(marginL, ty, marginR, ty, line)
        ty += 16
        val small19 = !inv.isSmallBusiness
        // columns
        val colDesc = marginL
        val colQty = 320f
        val colUnit = 360f
        val colPrice = 410f
        val colVat = 470f
        val colTotal = marginR
        h.textSize = 9f
        c.drawText(res.getString(R.string.pdf_col_desc), colDesc, ty, h)
        h.textAlign = Paint.Align.RIGHT
        c.drawText(res.getString(R.string.pdf_col_qty), colUnit - 4, ty, h)
        h.textAlign = Paint.Align.LEFT
        c.drawText(res.getString(R.string.pdf_col_unit), colUnit, ty, h)
        h.textAlign = Paint.Align.RIGHT
        c.drawText(res.getString(R.string.pdf_col_price), colVat - 6, ty, h)
        if (small19) c.drawText(res.getString(R.string.pdf_col_vat), colTotal - 60, ty, h)
        c.drawText(res.getString(R.string.pdf_col_total), colTotal, ty, h)
        h.textAlign = Paint.Align.LEFT
        h.textSize = 10f
        ty += 8
        c.drawLine(marginL, ty, marginR, ty, line)
        ty += 16

        full.items.sortedBy { it.position }.forEach { item ->
            val lineNet = InvoiceCalc.lineNet(item)
            c.drawText(ellipsize(item.description, body, colQty - colDesc - 8), colDesc, ty, body)
            body.textAlign = Paint.Align.RIGHT
            c.drawText(Money.plain(item.quantity, locale), colUnit - 4, ty, body)
            body.textAlign = Paint.Align.LEFT
            c.drawText(item.unit, colUnit, ty, body)
            body.textAlign = Paint.Align.RIGHT
            c.drawText(Money.format(item.unitPrice, locale), colVat - 6, ty, body)
            if (small19) c.drawText("${(item.vatRate * 100).toInt()}%", colTotal - 60, ty, body)
            c.drawText(Money.format(lineNet, locale), colTotal, ty, body)
            body.textAlign = Paint.Align.LEFT
            ty += 18
        }

        ty += 4
        c.drawLine(330f, ty, marginR, ty, line)
        ty += 18

        // Totals
        val totals = InvoiceCalc.totals(full.items, inv.isSmallBusiness)
        fun totalRow(label: String, value: String, bold: Boolean = false) {
            val p = if (bold) h else body
            p.textAlign = Paint.Align.LEFT
            c.drawText(label, 360f, ty, p)
            p.textAlign = Paint.Align.RIGHT
            c.drawText(value, marginR, ty, p)
            p.textAlign = Paint.Align.LEFT
            ty += if (bold) 20 else 16
        }
        totalRow(res.getString(R.string.total_net), Money.format(totals.net, locale))
        if (small19) {
            totals.vatGroups.forEach { g ->
                totalRow("${res.getString(R.string.total_vat)} ${(g.rate * 100).toInt()}%", Money.format(g.vat, locale))
            }
        }
        c.drawLine(360f, ty - 6, marginR, ty - 6, line)
        totalRow(res.getString(R.string.total_gross), Money.format(totals.gross, locale), bold = true)

        // Small business note
        if (inv.isSmallBusiness) {
            ty += 14
            c.drawText(res.getString(R.string.small_business_note), marginL, ty, small)
        }

        // Notes
        inv.notes?.takeIf { it.isNotBlank() }?.let {
            ty += 22
            c.drawText(it, marginL, ty, body)
        }

        // Footer: payment terms + bank details
        var fy = pageH - 96f
        c.drawLine(marginL, fy, marginR, fy, line)
        fy += 16
        if (inv.type == InvoiceType.INVOICE) {
            c.drawText(res.getString(R.string.pdf_payment_terms, Dates.format(inv.dueDate, locale)), marginL, fy, small)
            fy += 14
        }
        c.drawText(res.getString(R.string.pdf_bank_details), marginL, fy, h.apply { textSize = 9f })
        fy += 12
        c.drawText("IBAN: ${settings.iban}    BIC: ${settings.bic}", marginL, fy, small)
        fy += 14
        c.drawText(res.getString(R.string.pdf_thank_you), marginL, fy, small.apply { color = accent })
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var t = text
        while (t.isNotEmpty() && paint.measureText("$t…") > maxWidth) t = t.dropLast(1)
        return "$t…"
    }

    private fun sanitize(name: String): String = name.replace(Regex("[^A-Za-z0-9_-]"), "_")

    @Suppress("DEPRECATION")
    private fun localizedResources(locale: Locale): android.content.res.Resources {
        val conf = android.content.res.Configuration(context.resources.configuration)
        conf.setLocale(locale)
        return context.createConfigurationContext(conf).resources
    }
}
