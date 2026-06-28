package com.mihara.billio.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val INVOICES = "invoices"
    const val QUOTES = "quotes"
    const val CLIENTS = "clients"
    const val SETTINGS = "settings"

    // invoiceForm?id=0&type=INVOICE
    const val INVOICE_FORM = "invoiceForm"
    fun invoiceForm(id: Long = 0, type: String = "INVOICE") = "$INVOICE_FORM?id=$id&type=$type"

    const val INVOICE_DETAIL = "invoiceDetail"
    fun invoiceDetail(id: Long) = "$INVOICE_DETAIL/$id"

    const val CLIENT_FORM = "clientForm"
    fun clientForm(id: Long = 0) = "$CLIENT_FORM?id=$id"

    const val CLIENT_DETAIL = "clientDetail"
    fun clientDetail(id: Long) = "$CLIENT_DETAIL/$id"
}

/** Top-level destinations that show the bottom navigation bar. */
val topLevelRoutes = setOf(
    Routes.DASHBOARD, Routes.INVOICES, Routes.QUOTES, Routes.CLIENTS, Routes.SETTINGS
)
