package org.example.project.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import org.example.project.MainActivity
import org.example.project.R
import org.example.project.db.DatabaseDriverFactory
import org.example.project.db.createWarrantyRepository
import org.example.project.model.PolicyDeadline
import org.example.project.model.kindNoun
import org.example.project.model.policyDeadlines
import org.example.project.model.todayEpochDay

private const val CHANNEL_ID = "deadline_reminders"
private const val UNIQUE_WORK_NAME = "deadline_reminder_check"
private val reminderThresholds = setOf(7, 3, 1, 0)

class DeadlineReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val repository = createWarrantyRepository(DatabaseDriverFactory(context))
        val warranties = repository.observeAll().first()
        val today = todayEpochDay()
        val due = policyDeadlines(warranties, today).filter { it.daysLeft in reminderThresholds }

        ensureChannel(context)
        due.forEach { deadline -> notify(context, deadline) }
        return Result.success()
    }

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Deadline reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Warranty, replacement and return deadlines coming up"
        }
        manager.createNotificationChannel(channel)
    }

    private fun notify(context: Context, deadline: PolicyDeadline) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val noun = deadline.policy?.kind?.let { kindNoun(it) } ?: "warranty"
        val body = when {
            deadline.daysLeft <= 0 -> "Its $noun ends today."
            deadline.daysLeft == 1 -> "Its $noun ends tomorrow."
            else -> "Its $noun ends in ${deadline.daysLeft} days."
        }
        val openIntent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(deadline.warranty.productName)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (deadline.warranty.id + (deadline.policy?.id ?: "warranty") + deadline.daysLeft).hashCode()
        androidx.core.app.NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}

fun scheduleDeadlineReminders(context: Context) {
    val request = PeriodicWorkRequestBuilder<DeadlineReminderWorker>(24, TimeUnit.HOURS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
