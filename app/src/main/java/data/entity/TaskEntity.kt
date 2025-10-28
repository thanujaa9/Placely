package com.example.placely.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines the structure for storing To-Do Tasks.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    // Optional deadline stored as a Long
    val deadline: Long?,
    // Status of the task
    val isCompleted: Boolean = false,
    // Priority: "High" or "Low"
    val priority: String = "Low"
)