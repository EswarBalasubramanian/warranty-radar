package org.example.project.model

enum class Currency(val symbol: String, val label: String) {
    GBP("£", "£ GBP"),
    INR("₹", "₹ INR")
}

/** Safe parse: unknown/legacy values (rows saved before this field existed) fall back to GBP. */
fun currencyFrom(raw: String?): Currency =
    Currency.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: Currency.GBP
