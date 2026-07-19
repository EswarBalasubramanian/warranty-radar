package org.example.project.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

private val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

fun todayEpochDay(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays()

fun epochDayFromMillis(epochMillis: Long): Int =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.UTC).date.toEpochDays()

fun formatEpochDayLabel(epochDay: Int): String {
    val date = LocalDate.fromEpochDays(epochDay)
    return "${date.dayOfMonth} ${monthNames[date.monthNumber - 1]} ${date.year}"
}

/**
 * Tolerant parser for the date labels users type or scans produce:
 * "18 Jul 2026", "July 18, 2026", "18/07/2026", "18-07-26", "2026-07-18".
 * Ambiguous numeric dates are read day-first (UK style, matching the app's currency).
 */
fun parseFlexibleDateLabel(label: String): LocalDate? {
    val text = label.trim()
    if (text.isEmpty()) return null

    fun monthFrom(name: String): Int? {
        val prefix = name.take(3).lowercase()
        val index = monthNames.indexOfFirst { it.lowercase() == prefix }
        return if (index >= 0) index + 1 else null
    }

    fun yearFrom(raw: String): Int = raw.toInt().let { if (it < 100) 2000 + it else it }

    fun localDateOrNull(year: Int, month: Int, day: Int): LocalDate? =
        try {
            LocalDate(year, month, day)
        } catch (_: IllegalArgumentException) {
            null
        }

    Regex("""(\d{1,2})\s+([A-Za-z]{3,9})\.?,?\s+(\d{2,4})""").find(text)?.let { m ->
        val month = monthFrom(m.groupValues[2]) ?: return@let
        return localDateOrNull(yearFrom(m.groupValues[3]), month, m.groupValues[1].toInt())
    }
    Regex("""([A-Za-z]{3,9})\.?\s+(\d{1,2}),?\s+(\d{2,4})""").find(text)?.let { m ->
        val month = monthFrom(m.groupValues[1]) ?: return@let
        return localDateOrNull(yearFrom(m.groupValues[3]), month, m.groupValues[2].toInt())
    }
    Regex("""(\d{4})-(\d{1,2})-(\d{1,2})""").find(text)?.let { m ->
        return localDateOrNull(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
    }
    Regex("""(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{2,4})""").find(text)?.let { m ->
        val first = m.groupValues[1].toInt()
        val second = m.groupValues[2].toInt()
        val year = yearFrom(m.groupValues[3])
        val (day, month) = if (first > 12 && second <= 12) first to second
        else if (second > 12 && first <= 12) second to first
        else first to second
        return localDateOrNull(year, month, day)
    }
    return null
}

fun kindLabel(kind: PolicyKind): String = when (kind) {
    PolicyKind.Warranty -> "Warranty"
    PolicyKind.Replacement -> "Replacement"
    PolicyKind.Return -> "Return"
    PolicyKind.Service -> "Service"
    PolicyKind.Repair -> "Repair"
    PolicyKind.Support -> "Support"
    PolicyKind.Protection -> "Protection"
    PolicyKind.Other -> "Coverage"
}

/** How the policy reads inside a sentence: "Your kettle's return window ends tomorrow". */
fun kindNoun(kind: PolicyKind): String = when (kind) {
    PolicyKind.Warranty -> "warranty"
    PolicyKind.Replacement -> "replacement window"
    PolicyKind.Return -> "return window"
    PolicyKind.Service -> "service cover"
    PolicyKind.Repair -> "repair cover"
    PolicyKind.Support -> "support plan"
    PolicyKind.Protection -> "protection plan"
    PolicyKind.Other -> "coverage"
}

fun durationLabel(days: Int): String = when {
    days % 365 == 0 && days >= 365 -> if (days == 365) "1 year" else "${days / 365} years"
    days % 30 == 0 && days >= 30 -> if (days == 30) "1 month" else "${days / 30} months"
    days % 7 == 0 && days >= 14 -> "${days / 7} weeks"
    days == 1 -> "1 day"
    else -> "$days days"
}

fun CoveragePolicy.daysLeft(today: Int): Int? = endEpochDay?.let { it - today }

/** A draft's deadline once the purchase date is known: explicit date wins, else purchase + duration. */
fun resolvedEndEpochDay(draft: PolicyDraft, purchaseEpochDay: Int): Int? =
    draft.endEpochDay ?: draft.durationDays?.let { purchaseEpochDay + it }

data class PolicyDeadline(val warranty: Warranty, val policy: CoveragePolicy?, val daysLeft: Int) {
    val policyTitle: String get() = policy?.title ?: "Coverage deadline"
}

/**
 * Every dated policy across the library, flattened for the calendar — one entry
 * per deadline, not per product, since a purchase can have several clocks running.
 * Recently-ended deadlines (last 7 days) are kept so users still see what they missed.
 * Warranties with no policies fall back to their stored urgencyDays.
 */
fun policyDeadlines(warranties: List<Warranty>, today: Int): List<PolicyDeadline> =
    warranties.flatMap { warranty ->
        val dated = warranty.policies.mapNotNull { policy ->
            policy.daysLeft(today)?.let { days -> PolicyDeadline(warranty, policy, days) }
        }.filter { it.daysLeft >= -7 }
        if (dated.isNotEmpty()) dated
        else if (warranty.policies.isEmpty() && warranty.urgencyDays != null) {
            listOf(PolicyDeadline(warranty, null, warranty.urgencyDays))
        } else emptyList()
    }.sortedBy { it.daysLeft }

/** The next deadline still ahead of us, if any, as (policy, days left). */
fun nearestPolicyDeadline(policies: List<CoveragePolicy>, today: Int): Pair<CoveragePolicy, Int>? =
    policies.mapNotNull { policy -> policy.daysLeft(today)?.takeIf { it >= 0 }?.let { policy to it } }
        .minByOrNull { it.second }

/**
 * Recomputes urgency and status from the policies each time data is read, so
 * reminders stay live instead of freezing at whatever was true on save day.
 * Warranties without policies (legacy rows, seeds) keep their stored values.
 */
fun Warranty.withLiveStatus(today: Int): Warranty {
    if (policies.isEmpty()) return this
    val daysLeftValues = policies.mapNotNull { it.daysLeft(today) }
    val urgency = daysLeftValues.filter { it >= 0 }.minOrNull()
    val status = when {
        urgency != null -> warrantyStatusLabel.ifBlank { "Protected" }
        daysLeftValues.isNotEmpty() -> "Cover ended"
        else -> warrantyStatusLabel.ifBlank { "Protected" }
    }
    return copy(urgencyDays = urgency, warrantyStatusLabel = status)
}

/**
 * A nudge about what the user should actually do, phrased per policy type —
 * replacement windows want the product tested now, warranties want repairs booked.
 */
fun policyActionHint(kind: PolicyKind, daysLeft: Int?): String? {
    if (daysLeft == null || daysLeft < 0) return null
    return when (kind) {
        PolicyKind.Return, PolicyKind.Replacement -> when {
            daysLeft <= 3 -> "Last chance — check the item today"
            daysLeft <= 14 -> "Test everything while you can still send it back"
            else -> null
        }
        PolicyKind.Warranty, PolicyKind.Protection, PolicyKind.Service,
        PolicyKind.Repair, PolicyKind.Support -> when {
            daysLeft <= 7 -> "Book any repairs now — cover ends this week"
            daysLeft <= 30 -> "Worth a check-up before the cover ends"
            else -> null
        }
        PolicyKind.Other -> if (daysLeft <= 7) "Deadline this week — worth a look" else null
    }
}
