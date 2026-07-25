package org.example.project

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import org.example.project.db.DatabaseDriverFactory
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

        scheduleDeadlineReminders(applicationContext)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            App(DatabaseDriverFactory(applicationContext), startScreen)
        }
    }

    companion object {
        const val ACTION_ADD_RECEIPT = "org.example.project.ADD_RECEIPT"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(DatabaseDriverFactory(LocalContext.current))
}