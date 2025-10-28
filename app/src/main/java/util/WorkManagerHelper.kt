package com.example.placely.util

import android.content.Context
import androidx.work.*
import com.example.placely.data.entity.ReminderEntity
import com.example.placely.workers.NotificationWorker
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    /**
     * Schedule notifications for a reminder (both pre-alert AND deadline)
     */
    fun scheduleReminderNotification(context: Context, reminder: ReminderEntity) {
        val currentTime = System.currentTimeMillis()

        // 1. Schedule PRE-ALERT notification (if alert time is set)
        if (reminder.notificationAlert > 0) {
            val preAlertTime = reminder.dateTime - reminder.notificationAlert

            if (preAlertTime > currentTime) {
                val preAlertDelay = preAlertTime - currentTime

                val preAlertData = Data.Builder()
                    .putInt("reminderId", reminder.id)
                    .putString("title", reminder.title)
                    .putString("description", reminder.description)
                    .putString("type", reminder.type)
                    .putBoolean("isDeadline", false) // This is a pre-alert
                    .build()

                val preAlertRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(preAlertDelay, TimeUnit.MILLISECONDS)
                    .setInputData(preAlertData)
                    .addTag("reminder_${reminder.id}_prealert")
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "reminder_prealert_${reminder.id}",
                        ExistingWorkPolicy.REPLACE,
                        preAlertRequest
                    )
            }
        }

        // 2. ALWAYS schedule DEADLINE notification (at the actual event time)
        if (reminder.dateTime > currentTime) {
            val deadlineDelay = reminder.dateTime - currentTime

            val deadlineData = Data.Builder()
                .putInt("reminderId", reminder.id)
                .putString("title", reminder.title)
                .putString("description", reminder.description)
                .putString("type", reminder.type)
                .putBoolean("isDeadline", true) // This is the deadline notification
                .build()

            val deadlineRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(deadlineDelay, TimeUnit.MILLISECONDS)
                .setInputData(deadlineData)
                .addTag("reminder_${reminder.id}_deadline")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "reminder_deadline_${reminder.id}",
                    ExistingWorkPolicy.REPLACE,
                    deadlineRequest
                )
        }
    }

    /**
     * Cancel a scheduled notification
     */
    fun cancelReminderNotification(context: Context, reminderId: Int) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("reminder_notification_$reminderId")

        // Also cancel the notification if it's already showing
        NotificationHelper.cancelNotification(context, reminderId)
    }

    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }
}