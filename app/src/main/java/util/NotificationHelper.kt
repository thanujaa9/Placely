package com.example.placely.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.placely.R

object NotificationHelper {

    private const val CHANNEL_ID = "placely_reminders"
    private const val CHANNEL_NAME = "Placement Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming tests, interviews, and deadlines"

    /**
     * Create notification channel (required for Android O and above)
     * Call this once when the app starts
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show a notification for a reminder (with permission check)
     */
    fun showReminderNotification(
        context: Context,
        reminderId: Int,
        title: String,
        description: String?,
        type: String,
        isDeadline: Boolean = false // true if this is the deadline notification
    ) {
        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return // Permission not granted, exit
            }
        }

        // Intent to open the app when notification is clicked
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderId", reminderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Different notification text based on whether it's a pre-alert or deadline
        val notificationTitle = if (isDeadline) "⏰ $title - NOW!" else "🔔 Upcoming: $title"
        val notificationText = if (isDeadline) {
            description ?: "Your $type is happening now!"
        } else {
            description ?: "You have an upcoming $type"
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500)) // Vibration pattern
            .build()

        try {
            // Show the notification (use different notification IDs for pre-alert vs deadline)
            val notificationId = if (isDeadline) reminderId else reminderId + 10000
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Cancel a scheduled notification
     */
    fun cancelNotification(context: Context, reminderId: Int) {
        NotificationManagerCompat.from(context).cancel(reminderId)
        NotificationManagerCompat.from(context).cancel(reminderId + 10000) // Also cancel pre-alert
    }
}