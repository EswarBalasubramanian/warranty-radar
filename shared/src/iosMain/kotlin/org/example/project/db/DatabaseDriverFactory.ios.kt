package org.example.project.db

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver? = null
}

actual fun deleteStoredFile(path: String) {}
