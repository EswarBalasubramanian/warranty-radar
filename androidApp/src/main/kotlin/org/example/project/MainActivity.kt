package org.example.project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import org.example.project.backup.exportWarrantiesToJson
import org.example.project.db.DatabaseDriverFactory
import org.example.project.model.Warranty
import org.example.project.notifications.ReminderPreferences
import org.example.project.notifications.scheduleDeadlineReminders

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val startScreen = if (intent?.action == ACTION_ADD_RECEIPT) {
            AppScreen.PasteReceipt
        } else {
            AppScreen.Home
        }
        val pendingWarrantyId = intent?.getStringExtra(EXTRA_WARRANTY_ID)

        scheduleDeadlineReminders(applicationContext)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val reminderPreferences = ReminderPreferences(applicationContext)

        setContent {
            var enabledThresholds by remember { mutableStateOf(reminderPreferences.getEnabledThresholds()) }
            App(
                DatabaseDriverFactory(applicationContext),
                startScreen,
                pendingEditWarrantyId = pendingWarrantyId,
                onViewPhoto = { path -> viewPhoto(path) },
                enabledReminderThresholds = enabledThresholds,
                onReminderThresholdsChange = { updated ->
                    enabledThresholds = updated
                    reminderPreferences.setEnabledThresholds(updated)
                },
                onExport = { warranties -> exportWarranties(warranties) }
            )
        }
    }

    private fun viewPhoto(path: String) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", File(path))
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/jpeg")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "View receipt photo"))
    }

    private fun exportWarranties(warranties: List<Warranty>) {
        val exportsDir = File(cacheDir, "exports").apply { mkdirs() }
        val file = File(exportsDir, "warranty-radar-export.json")
        file.writeText(exportWarrantiesToJson(warranties))
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Export your data"))
    }

    companion object {
        const val ACTION_ADD_RECEIPT = "org.example.project.ADD_RECEIPT"
        const val EXTRA_WARRANTY_ID = "org.example.project.EXTRA_WARRANTY_ID"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(DatabaseDriverFactory(LocalContext.current))
}