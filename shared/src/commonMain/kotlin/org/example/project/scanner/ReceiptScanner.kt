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

/**
 * Best-effort heuristics for pre-filling the receipt form from raw OCR text.
 * Intentionally conservative: returns null guesses rather than a wrong-looking value
 * when the text doesn't clearly contain that field.
 */
fun parseReceiptGuesses(rawText: String): ScannedReceipt {
    val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

    val guessedStore = lines.firstOrNull { line ->
        line.length in 2..40 && line.any { it.isLetter() }
    }

    val priceRegex = Regex("""[£$€]?\s?(\d{1,5}(?:[.,]\d{2}))""")
    val totalKeywords = listOf("total", "amount due", "amount", "balance", "grand total")
    val totalLineMatch = lines
        .firstOrNull { line -> totalKeywords.any { keyword -> line.contains(keyword, ignoreCase = true) } }
        ?.let { line -> priceRegex.find(line) }
    val guessedPrice = (totalLineMatch ?: priceRegex.findAll(rawText).maxByOrNull {
        it.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0
    })?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()

    val dateRegex = Regex(
        """\b(\d{1,2}[\/\-.]\d{1,2}[\/\-.]\d{2,4}|\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4})\b""",
        RegexOption.IGNORE_CASE
    )
    val guessedDate = dateRegex.find(rawText)?.value?.trim()

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
    Regex("""\breturn(?:able|s)?\b|\brefund(?:s|able)?\b|\bmoney[\s-]?back\b""", RegexOption.IGNORE_CASE) to PolicyKind.Return,
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
    """warrant|guarant|replacement|returnable|non[\s-]?returnable|return\s+policy|free\s+returns?|refund|money[\s-]?back|protection\s+plan|insurance|service\s+cent|extended\s+cover|repair""",
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
