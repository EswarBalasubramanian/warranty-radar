package org.example.project.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberReceiptScannerController(onScanned: (ScannedReceipt) -> Unit): ReceiptScannerController {
    val status = remember { mutableStateOf<ScanStatus>(ScanStatus.Idle) }
    val notSupported: () -> Unit = { status.value = ScanStatus.Error("Receipt scanning isn't available on this platform yet") }
    return remember {
        ReceiptScannerController(
            statusState = status,
            captureFromCamera = notSupported,
            pickFromGallery = notSupported,
            pickPdf = notSupported
        )
    }
}
