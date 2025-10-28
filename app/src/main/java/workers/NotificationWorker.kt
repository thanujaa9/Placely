package com.example.placely.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.placely.util.NotificationHelper

/**
 * Worker that handles showing reminder notifications at scheduled times
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get reminder details from input data
            val reminderId = inputData.getInt("reminderId", 0)
            val title = inputData.getString("title") ?: "Reminder"
            val description = inputData.getString("description")
            val type = inputData.getString("type") ?: "Event"
            val isDeadline = inputData.getBoolean("isDeadline", false)

            // Show the notification
            NotificationHelper.showReminderNotification(
                context = applicationContext,
                reminderId = reminderId,
                title = title,
                description = description,
                type = type,
                isDeadline = isDeadline
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}