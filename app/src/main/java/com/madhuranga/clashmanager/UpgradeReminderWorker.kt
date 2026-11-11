package com.madhuranga.clashmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UpgradeReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString("task_title") ?: "Upgrade"
        val channelId = "upgrade_completions"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Upgrade reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Clash Manager · Upgrade complete")
            .setContentText("$title should be finished. Check your village and update the task.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        return try {
            NotificationManagerCompat.from(applicationContext)
                .notify(inputData.getLong("task_id", System.currentTimeMillis()).toInt(), notification)
            Result.success()
        } catch (_: SecurityException) {
            // Android notification permission may be disabled. The task remains tracked in-app.
            Result.success()
        }
    }
}