package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.db.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val startScreen = if (intent?.action == ACTION_ADD_RECEIPT) {
            AppScreen.PasteReceipt
        } else {
            AppScreen.Home
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