package org.example.project.model

enum class ProductShape { Watch, Lamp, Laptop, Coffee, Other }

/**
 * Coarse bucket used only for presentation (icon colour, friendly nouns, sorting).
 * The authoritative policy wording always lives in [CoveragePolicy.title] and
 * [CoveragePolicy.notes], so a policy phrasing we've never seen still round-trips
 * intact as [Other] — no schema or enum change required to store it.
 */
enum class PolicyKind { Warranty, Replacement, Return, Service, Repair, Support, Protection, Other }

/** Safe parse: unknown values written by future app versions degrade to [PolicyKind.Other]. */
fun policyKindFrom(raw: String): PolicyKind =
    PolicyKind.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PolicyKind.Other

enum class PolicySource { Scanned, Manual }

fun policySourceFrom(raw: String): PolicySource =
    PolicySource.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PolicySource.Manual

/**
 * One warranty / replacement / return / service promise attached to a purchase.
 * A purchase can hold any number of these, in any combination.
 */
data class CoveragePolicy(
    val id: String,
    val kind: PolicyKind,
    val title: String,
    val provider: String? = null,
    val durationDays: Int? = null,
    val endEpochDay: Int? = null,
    val endDateLabel: String? = null,
    val notes: String? = null,
    val source: PolicySource = PolicySource.Manual
)

/**
 * A policy while it's still being assembled on the add-receipt form —
 * scans and manual edits both produce drafts, which get resolved against
 * the purchase date when the receipt is saved.
 */
data class PolicyDraft(
    val kind: PolicyKind,
    val title: String,
    val provider: String? = null,
    val durationDays: Int? = null,
    val endEpochDay: Int? = null,
    val notes: String? = null,
    val fromScan: Boolean = false
)

data class Warranty(
    val id: String,
    val productName: String,
    val store: String,
    val category: String,
    val purchaseDateLabel: String,
    val warrantyStatusLabel: String,
    val urgencyDays: Int?,
    val price: Double?,
    val shape: ProductShape,
    val warrantyEndDateLabel: String? = null,
    val policies: List<CoveragePolicy> = emptyList(),
    val photoPath: String? = null
)
