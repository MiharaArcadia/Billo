package com.mihara.billio.data.model

/** Document type. Quotes and invoices share the same storage. */
enum class InvoiceType { INVOICE, QUOTE, CREDIT_NOTE }

/**
 * Lifecycle status. INVOICE documents use DRAFT/SENT/PAID/OVERDUE/CANCELLED,
 * QUOTE documents use DRAFT/SENT/ACCEPTED/REJECTED.
 */
enum class InvoiceStatus { DRAFT, SENT, PAID, OVERDUE, CANCELLED, ACCEPTED, REJECTED }

/** Tax regime of the issuer. */
enum class TaxMode { SMALL_BUSINESS, REGULAR }
