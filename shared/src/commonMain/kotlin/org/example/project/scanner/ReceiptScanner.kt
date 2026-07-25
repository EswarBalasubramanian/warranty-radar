package org.example.project.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import org.example.project.model.PolicyDraft
import org.example.project.model.PolicyKind
import org.example.project.model.parseFlexibleDateLabel

const val MAX_RECEIPT_FILE_SIZE_BYTES = 10L * 1024 * 1024

data class ScannedReceipt(
    val rawText: String,
    val guessedStore: String? = null,
    val guessedPrice: Double? = null,
    val guessedPurchaseDateLabel: String? = null,
    val guessedPolicies: List<PolicyDraft> = emptyList()
)

sealed interface ScanStatus {
    data object Idle : ScanStatus
    data object Processing : ScanStatus
    data class Error(val message: String) : ScanStatus
}

class ReceiptScannerController(
    val statusState: State<ScanStatus>,
    val captureFromCamera: () -> Unit,
    val pickFromGallery: () -> Unit,
    val pickPdf: () -> Unit
) {
    val status: ScanStatus get() = statusState.value
}

/**
 * Provides platform pickers (camera / gallery / PDF document) and on-device OCR.
 * Real implementation is Android-only; other targets return a controller whose
 * actions surface a "not supported" error so the multiplatform build still compiles.
 */
@Composable
expect fun rememberReceiptScannerController(onScanned: (ScannedReceipt) -> Unit): ReceiptScannerController

// Matches a monetary token as a whole so it can be normalized afterward. Three
// alternatives, tried in order: a currency-symbol-prefixed number (decimals
// optional — many receipts show whole-currency totals with no cents), a
// thousands-grouped number with no symbol ("7,995" / "1,234.56"), or a plain
// two-decimal amount ("349.00"). Matching the full token (instead of a single
// capture group) avoids truncating grouped digits like "7,995" into "7,99".
private val priceRegex = Regex(
    """[£$€₹]\s?\d{1,3}(?:,\d{2,3})*(?:\.\d{1,2})?|\d{1,3}(?:,\d{2,3})+(?:\.\d{1,2})?|\d+\.\d{2}"""
)

private fun MatchResult.priceValue(): Double? =
    value.trim().trimStart('£', '$', '€', '₹').trim().replace(",", "").toDoubleOrNull()

// Store names sit in the first few header lines. Anything that looks like an
// address, phone number, receipt/order number, URL, date, price, or common
// web/product-page chrome (breadcrumbs, media badges, marketing copy) on its
// own line is almost certainly not the store name, even if it appears early.
private val storeExclusionRegex = Regex(
    """receipt|invoice|order\s*#|order\s*no|tel[:.]|phone|fax|www\.|http|@|customer\s+copy|thank\s+you|cashier|register|store\s*#|transaction|\bvat\b|\babn\b|\bein\b|""" +
        """\bvideos?\b|\bphotos?\b|\bprime\b|\btomorrow\b|\bemi\b|\bvisit\s+the\b|\bsearch\s+this\s+page\b|\bask\b|\brufus\b|\bbought\s+in\s+past\s+month\b|\blimited\s+time\s+deal\b|\bprice\s+history\b|>""",
    RegexOption.IGNORE_CASE
)
private val addressCueRegex = Regex(
    """\b(street|st\.|road|rd\.|avenue|ave\.|blvd|boulevard|lane|ln\.|suite|ste\.|floor|fl\.|drive|dr\.)\b""",
    RegexOption.IGNORE_CASE
)
private val phoneRegex = Regex("""\(?\d{3}\)?[\s.-]\d{3}[\s.-]\d{4}""")
private val dateLikeRegex = Regex("""\d{1,4}[/\-.]\d{1,2}[/\-.]\d{1,4}""")

private fun looksLikeStoreName(line: String): Boolean {
    if (line.length !in 2..40) return false
    if (!line.any { it.isLetter() }) return false
    if (line.count { it.isDigit() } > line.length / 3) return false
    if (storeExclusionRegex.containsMatchIn(line)) return false
    if (addressCueRegex.containsMatchIn(line)) return false
    if (phoneRegex.containsMatchIn(line)) return false
    if (dateLikeRegex.containsMatchIn(line)) return false
    if (priceRegex.containsMatchIn(line)) return false
    return true
}

// Invoices frequently state the seller explicitly via a label — "Sold By :" is the
// standard Amazon-style pattern. The seller name follows either right after the
// colon on the same line, or (more often) on the next line entirely. This is a much
// stronger signal than "first plausible-looking line", so it's tried first.
private val storeCueRegex = Regex("""^(?:sold\s*by|seller|vendor|merchant)\s*:?\s*(.*)$""", RegexOption.IGNORE_CASE)

private fun findStoreByCue(lines: List<String>): String? {
    lines.forEachIndexed { index, line ->
        val match = storeCueRegex.find(line) ?: return@forEachIndexed
        val inline = match.groupValues[1].trim()
        if (inline.isNotEmpty() && looksLikeStoreName(inline)) return inline
        val next = lines.getOrNull(index + 1)
        if (next != null && looksLikeStoreName(next)) return next
    }
    return null
}

// "Total" lines are ranked by how specific/authoritative the keyword is, and
// subtotal/tax lines are excluded outright since "subtotal" also contains "total".
private val subtotalRegex = Regex("""sub[\s-]?total|\btax\b""", RegexOption.IGNORE_CASE)
private val totalPriorityKeywords = listOf(
    "grand total", "total due", "amount due", "balance due", "total paid", "total amount", "total", "amount", "balance"
)

// Purchase date should come from a line that talks about the purchase/transaction
// itself, never from an expiry/warranty/best-before line (those are policy end dates).
private val purchaseDateCueRegex = Regex(
    """\b(date|purchase|sold|transaction|order\s*date|invoice\s*date)\b""",
    RegexOption.IGNORE_CASE
)
private val purchaseDateExclusionRegex = Regex(
    """\bexp(?:iry|ires?|iration)?\b|valid\s*until|warrant|due\s*date|best\s*before|return\s*by|expires?\s*on""",
    RegexOption.IGNORE_CASE
)
private val dateRegex = Regex(
    """\b(\d{4}-\d{1,2}-\d{1,2}|\d{1,2}[\/\-.]\d{1,2}[\/\-.]\d{2,4}|\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4})\b""",
    RegexOption.IGNORE_CASE
)

/**
 * Best-effort heuristics for pre-filling the receipt form from raw OCR text.
 * Intentionally conservative: returns null guesses rather than a wrong-looking value
 * when the text doesn't clearly contain that field.
 */
fun parseReceiptGuesses(rawText: String): ScannedReceipt {
    val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

    val guessedStore = findStoreByCue(lines)
        ?: lines.take(8).firstOrNull { looksLikeStoreName(it) }
        ?: lines.firstOrNull { looksLikeStoreName(it) }

    // The keyword must lead the line ("Total: $50.00", "Grand Total  $50.00") rather than
    // appear anywhere in it, otherwise marketing copy like "Amazon Pay Balance" would
    // falsely match the bare "balance" keyword. Tabular rows often show several prices on
    // the total line itself (e.g. "TOTAL: $5.33 tax $349.00 total") — the actual total is
    // always the largest of them, since tax/discount components can't exceed the total.
    val nonSubtotalLines = lines.filterNot { subtotalRegex.containsMatchIn(it) }
    val totalLineMatch = totalPriorityKeywords
        .asSequence()
        .mapNotNull { keyword ->
            nonSubtotalLines.firstNotNullOfOrNull { line ->
                if (line.startsWith(keyword, ignoreCase = true)) {
                    priceRegex.findAll(line).mapNotNull { it.priceValue() }.maxOrNull()
                } else null
            }
        }
        .firstOrNull()
    val guessedPrice = totalLineMatch
        ?: priceRegex.findAll(rawText).mapNotNull { it.priceValue() }.maxOrNull()

    val dateCandidateLines = lines.filterNot { purchaseDateExclusionRegex.containsMatchIn(it) }
    val guessedDate = (
        dateCandidateLines.firstOrNull { purchaseDateCueRegex.containsMatchIn(it) }?.let { dateRegex.find(it)?.value }
            ?: dateCandidateLines.firstNotNullOfOrNull { dateRegex.find(it)?.value }
            ?: dateRegex.find(rawText)?.value
        )?.trim()

    return ScannedReceipt(
        rawText = rawText,
        guessedStore = guessedStore,
        guessedPrice = guessedPrice,
        guessedPurchaseDateLabel = guessedDate,
        guessedPolicies = extractPolicyDrafts(rawText)
    )
}

// --- Coverage policy extraction -------------------------------------------------
//
// Deliberately open-ended: instead of matching a fixed set of known policy names
// ("6 Month Warranty", "10 Days Replacement"), any text segment that mentions a
// coverage concept is captured verbatim, classified into a coarse kind, and any
// duration or deadline found alongside is attached. Phrasings we've never seen
// still come through as a draft the user can keep or discard.

private val kindMatchers: List<Pair<Regex, PolicyKind>> = listOf(
    Regex("""\breplace(?:ment|ments|able|d)?\b|\bexchange\b""", RegexOption.IGNORE_CASE) to PolicyKind.Replacement,
    Regex("""\breturn(?:able|s)?\b|\brefund(?:s|able)?\b|\bmoney[\s-]?back\b|\bstore\s+credit\b""", RegexOption.IGNORE_CASE) to PolicyKind.Return,
    Regex("""\bprotection\b|\binsurance\b|\bdamage\s+cover""", RegexOption.IGNORE_CASE) to PolicyKind.Protection,
    Regex("""\bwarrant(?:y|ies|ee)\b|\bguarantee?d?\b|\bguaranty\b""", RegexOption.IGNORE_CASE) to PolicyKind.Warranty,
    Regex("""\brepair(?:s|ed)?\b""", RegexOption.IGNORE_CASE) to PolicyKind.Repair,
    Regex("""\bservice\b""", RegexOption.IGNORE_CASE) to PolicyKind.Service,
    Regex("""\bsupport\b|\bhelpline\b|\bcustomer\s+care\b""", RegexOption.IGNORE_CASE) to PolicyKind.Support,
    Regex("""\bcover(?:age|ed)?\b""", RegexOption.IGNORE_CASE) to PolicyKind.Other
)

// A segment only becomes a draft when it carries real policy substance, so page
// furniture like "Return to top" doesn't get captured.
private val strongPolicyPhrases = Regex(
    """warrant|guarant|replacement|returnable|non[\s-]?returnable|return\s+policy|free\s+returns?|refund|money[\s-]?back|store\s+credit|protection\s+plan|insurance|service\s+cent|extended\s+cover|repair""",
    RegexOption.IGNORE_CASE
)

private val numberWords = mapOf(
    "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5, "six" to 6,
    "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10, "eleven" to 11,
    "twelve" to 12, "fifteen" to 15, "thirty" to 30, "sixty" to 60, "ninety" to 90
)

private val durationRegex = Regex(
    """\b(\d{1,3}|${numberWords.keys.joinToString("|")})[\s-]*(day|week|month|year)s?\b""",
    RegexOption.IGNORE_CASE
)

private val deadlineCueRegex = Regex(
    """\b(?:till|until|by|ends?|ending|expires?|expiry|valid|through|up\s*to|before)\b""",
    RegexOption.IGNORE_CASE
)

private val policyDateRegex = Regex(
    """\b(\d{1,2}[/\-.]\d{1,2}[/\-.]\d{2,4}|\d{4}-\d{1,2}-\d{1,2}|\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?,?\s+\d{2,4}|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{1,2},?\s+\d{2,4})\b""",
    RegexOption.IGNORE_CASE
)

private const val MAX_POLICIES_PER_SCAN = 6

fun extractPolicyDrafts(rawText: String): List<PolicyDraft> {
    val segments = rawText.lines()
        .flatMap { it.split('•', '·', '|', ';') }
        .map { it.trim().trimStart('-', '*', '–', '—', ':').trim() }
        .filter { it.isNotBlank() }

    var drafts = emptyList<PolicyDraft>()
    for (segment in segments) {
        if (drafts.size >= MAX_POLICIES_PER_SCAN) break
        val kind = kindMatchers.firstOrNull { (regex, _) -> regex.containsMatchIn(segment) }?.second ?: continue

        val durationDays = durationRegex.find(segment)?.let { match ->
            val raw = match.groupValues[1].lowercase()
            val amount = raw.toIntOrNull() ?: numberWords[raw] ?: return@let null
            val unitDays = when (match.groupValues[2].lowercase()) {
                "day" -> 1
                "week" -> 7
                "month" -> 30
                else -> 365
            }
            amount * unitDays
        }

        val endEpochDay = if (deadlineCueRegex.containsMatchIn(segment)) {
            policyDateRegex.find(segment)?.value?.let { parseFlexibleDateLabel(it)?.toEpochDays() }
        } else null

        val hasSubstance = durationDays != null || endEpochDay != null || strongPolicyPhrases.containsMatchIn(segment)
        if (!hasSubstance) continue

        val cleaned = segment.replace(Regex("""\s+"""), " ").trim().trimEnd('.', ',')
        val draft = PolicyDraft(
            kind = kind,
            title = if (cleaned.length <= 90) cleaned else cleaned.take(87) + "…",
            provider = detectProvider(segment),
            durationDays = durationDays,
            endEpochDay = endEpochDay,
            notes = cleaned.take(300),
            fromScan = true
        )
        drafts = mergePolicyDrafts(drafts, listOf(draft))
    }
    return drafts
}

private fun detectProvider(segment: String): String? {
    val lower = segment.lowercase()
    return when {
        "amazon" in lower -> "Amazon"
        "service centre" in lower || "service center" in lower -> "Service centre"
        "manufacturer" in lower -> "Manufacturer"
        "seller" in lower -> "Seller"
        "brand" in lower -> "Brand"
        else -> null
    }
}

/**
 * Merges policy drafts from separate scans (or repeated lines in one scan) into a
 * single record set — the details of one policy often span multiple screenshots.
 * Two drafts are considered the same policy when kind matches and either the
 * wording overlaps or they name the same duration; the merged draft keeps the
 * richest version of every field.
 */
fun mergePolicyDrafts(existing: List<PolicyDraft>, incoming: List<PolicyDraft>): List<PolicyDraft> {
    val result = existing.toMutableList()
    for (candidate in incoming) {
        val index = result.indexOfFirst { it.isSamePolicyAs(candidate) }
        if (index >= 0) result[index] = result[index].enrichedWith(candidate) else result += candidate
    }
    return result
}

private fun normalizedTitle(title: String): String = title.lowercase().filter { it.isLetterOrDigit() }

private fun PolicyDraft.isSamePolicyAs(other: PolicyDraft): Boolean {
    if (kind != other.kind) return false
    val mine = normalizedTitle(title)
    val theirs = normalizedTitle(other.title)
    return mine == theirs ||
        (mine.isNotEmpty() && theirs.isNotEmpty() && (mine in theirs || theirs in mine)) ||
        (durationDays != null && durationDays == other.durationDays)
}

private fun PolicyDraft.enrichedWith(other: PolicyDraft): PolicyDraft = copy(
    title = title.ifBlank { other.title },
    provider = provider ?: other.provider,
    durationDays = durationDays ?: other.durationDays,
    endEpochDay = endEpochDay ?: other.endEpochDay,
    notes = listOfNotNull(notes, other.notes).maxByOrNull { it.length },
    fromScan = fromScan || other.fromScan
)
