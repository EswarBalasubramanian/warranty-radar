package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.db.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory()) }