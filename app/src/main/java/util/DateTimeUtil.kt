package com.example.placely.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Utility object for formatting date and time values
 * Provides extension functions for Long timestamps
 */
object DateTimeUtil {

    /**
     * Converts a timestamp (Long) to a friendly date string
     * Example: "Dec 25, 2024"
     */
    fun Long.toFriendlyDate(): String {
        if (this <= 0) return "Select Date"

        val instant = Instant.ofEpochMilli(this)
        val dateTime = instant.atZone(ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        return dateTime.format(formatter)
    }

    /**
     * Converts a timestamp (Long) to a friendly time string
     * Example: "02:30 PM"
     */
    fun Long.toFriendlyTime(): String {
        if (this <= 0) return "Select Time"

        val instant = Instant.ofEpochMilli(this)
        val dateTime = instant.atZone(ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        return dateTime.format(formatter)
    }

    /**
     * Converts a timestamp (Long) to a combined friendly date and time string
     * Example: "Dec 25, 2024 at 02:30 PM"
     */
    fun Long.toFriendlyDateTime(): String {
        if (this <= 0) return "No date set"

        val instant = Instant.ofEpochMilli(this)
        val dateTime = instant.atZone(ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return dateTime.format(formatter)
    }

    /**
     * Converts a timestamp (Long) to a short date format
     * Example: "12/25/24"
     */
    fun Long.toShortDate(): String {
        if (this <= 0) return ""

        val instant = Instant.ofEpochMilli(this)
        val dateTime = instant.atZone(ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.getDefault())
        return dateTime.format(formatter)
    }

    /**
     * Converts a timestamp (Long) to relative time
     * Example: "2 hours from now", "Yesterday", "In 3 days"
     */
    fun Long.toRelativeTime(): String {
        val now = System.currentTimeMillis()
        val diff = this - now

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 1 -> "In ${days.toInt()} days"
            days == 1L -> "Tomorrow"
            hours > 1 -> "In ${hours.toInt()} hours"
            hours == 1L -> "In 1 hour"
            minutes > 1 -> "In ${minutes.toInt()} minutes"
            minutes == 1L -> "In 1 minute"
            seconds > 0 -> "In a few seconds"
            days < -1 -> "${(-days).toInt()} days ago"
            days == -1L -> "Yesterday"
            hours < -1 -> "${(-hours).toInt()} hours ago"
            else -> "Just now"
        }
    }

    /**
     * Checks if the timestamp is in the past
     */
    fun Long.isPast(): Boolean {
        return this < System.currentTimeMillis()
    }

    /**
     * Checks if the timestamp is today
     */
    fun Long.isToday(): Boolean {
        val instant = Instant.ofEpochMilli(this)
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDate()

        val today = Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(ZoneId.systemDefault()).toLocalDate()

        return dateTime == today
    }

    /**
     * Checks if the timestamp is tomorrow
     */
    fun Long.isTomorrow(): Boolean {
        val instant = Instant.ofEpochMilli(this)
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDate()

        val tomorrow = Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1)

        return dateTime == tomorrow
    }

    /**
     * Alias functions for compatibility
     */
    fun formatDateTime(timestamp: Long): String = timestamp.toFriendlyDateTime()
    fun formatDateOnly(timestamp: Long): String = timestamp.toFriendlyDate()
}