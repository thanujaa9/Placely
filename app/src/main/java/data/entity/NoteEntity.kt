package com.example.placely.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines the structure for storing short Notes (Revisions, HR questions).
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val content: String,
    // Flag to pin important notes to the top of the list
    val isPinned: Boolean = false,
    // Timestamp helps in sorting notes by creation time
    val timestamp: Long = System.currentTimeMillis()
)