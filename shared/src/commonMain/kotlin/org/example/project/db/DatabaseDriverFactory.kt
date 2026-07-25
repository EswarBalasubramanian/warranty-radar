package org.example.project.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver?
}

expect fun deleteStoredFile(path: String)
