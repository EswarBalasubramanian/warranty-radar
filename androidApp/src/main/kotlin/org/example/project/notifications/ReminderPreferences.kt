package org.example.project.notifications

import android.content.Context

private const val PREFS_NAME = "reminder_preferences"
private const val KEY_THRESHOLDS = "enabled_thresholds"
val defaultReminderThresholds = setOf(7, 3, 1, 0)

class ReminderPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getEnabledThresholds(): Set<Int> =
        prefs.getStringSet(KEY_THRESHOLDS, null)?.mapNotNull { it.toIntOrNull() }?.toSet()
            ?: defaultReminderThresholds

    fun setEnabledThresholds(thresholds: Set<Int>) {
        prefs.edit().putStringSet(KEY_THRESHOLDS, thresholds.map { it.toString() }.toSet()).apply()
    }
}
