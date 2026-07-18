package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.db.DatabaseDriverFactory

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App(DatabaseDriverFactory())
    }
}