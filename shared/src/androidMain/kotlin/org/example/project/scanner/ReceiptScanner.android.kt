package org.example.project.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Composable
actual fun rememberReceiptScannerController(onScanned: (ScannedReceipt) -> Unit): ReceiptScannerController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val status = remember { mutableStateOf<ScanStatus>(ScanStatus.Idle) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun handleUri(uri: Uri, isPdf: Boolean) {
        scope.launch {
            status.value = ScanStatus.Processing
            try {
                val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                if (size > MAX_RECEIPT_FILE_SIZE_BYTES) {
                    status.value = ScanStatus.Error("That file is larger than 10 MB. Choose a smaller file.")
                    return@launch
                }
                val rawText = if (isPdf) recognizeTextFromPdf(context, uri) else recognizeTextFromImage(context, uri)
                if (rawText.isBlank()) {
                    status.value = ScanStatus.Error("Couldn't find any text in that file.")
                    return@launch
                }
                onScanned(parseReceiptGuesses(rawText))
                status.value = ScanStatus.Idle
            } catch (t: Throwable) {
                status.value = ScanStatus.Error(t.message ?: "Couldn't read that file.")
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleUri(it, isPdf = false) }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleUri(it, isPdf = true) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val uri = pendingCameraUri
        if (captured && uri != null) {
            handleUri(uri, isPdf = false)
        }
    }

    return ReceiptScannerController(
        statusState = status,
        captureFromCamera = {
            val uri = createTempImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        },
        pickFromGallery = { galleryLauncher.launch("image/*") },
        pickPdf = { pdfLauncher.launch(arrayOf("application/pdf")) }
    )
}

private fun createTempImageUri(context: Context): Uri {
    val capturesDir = File(context.cacheDir, "receipt_captures").apply { mkdirs() }
    val file = File(capturesDir, "receipt_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private suspend fun recognizeTextFromImage(context: Context, uri: Uri): String {
    val image = InputImage.fromFilePath(context, uri)
    return recognizeText(image)
}

private suspend fun recognizeTextFromPdf(context: Context, uri: Uri): String {
    val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return ""
    val builder = StringBuilder()
    descriptor.use { fd ->
        PdfRenderer(fd).use { renderer ->
            val pageCount = minOf(renderer.pageCount, 5)
            for (index in 0 until pageCount) {
                renderer.openPage(index).use { page ->
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    builder.append(recognizeText(InputImage.fromBitmap(bitmap, 0))).append("\n")
                    bitmap.recycle()
                }
            }
        }
    }
    return builder.toString()
}

private suspend fun recognizeText(image: InputImage): String = suspendCancellableCoroutine { continuation ->
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { visionText -> continuation.resumeWith(Result.success(visionText.text)) }
        .addOnFailureListener { error -> continuation.resumeWith(Result.failure(error)) }
}
