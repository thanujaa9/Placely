package com.example.placely.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.placely.data.dao.NoteDao
import com.example.placely.data.dao.ReminderDao
import com.example.placely.data.dao.TaskDao
import com.example.placely.data.entity.NoteEntity
import com.example.placely.data.entity.ReminderEntity
import com.example.placely.data.entity.TaskEntity

/**
 * The main database class for the Placely application.
 * It links all the Entities and DAOs together.
 *
 * It is an abstract class that Room generates the implementation for at compile time.
 */
@Database(
    // 1. List all the Entity classes (tables) that belong to this database
    entities = [ReminderEntity::class, NoteEntity::class, TaskEntity::class],
    // 2. The version number of the database. Must be incremented on schema changes.
    version = 1,
    // 3. Set to false to prevent exporting the schema into a folder
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Abstract functions to expose the DAOs.
    // Room will provide the implementation for these methods.
    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}
