package org.example.project.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

const val MAX_RECEIPT_FILE_SIZE_BYTES = 10L * 1024 * 1024

data class ScannedReceipt(
    val rawText: String,
    val guessedStore: String? = null,
    val guessedPrice: Double? = null,
    val guessedPurchaseDateLabel: String? = null
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
        guessedPurchaseDateLabel = guessedDate
    )
}
