package com.example.placely.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.placely.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Reminder table.
 * All functions are suspend functions because Room operations should run off the main thread.
 * Flow is used for observing real-time changes to the data.
 */
@Dao
interface ReminderDao {

    // Insert or replace a reminder (e.g., when adding a new one or updating an existing one)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    // Update an existing reminder
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    // Delete a reminder
    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    // Get all reminders, ordered by date and time (ascending)
    @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    // Get only upcoming reminders (where dateTime is greater than the current time)
    @Query("SELECT * FROM reminders WHERE dateTime >= :currentDate ORDER BY dateTime ASC")
    fun getUpcomingReminders(currentDate: Long): Flow<List<ReminderEntity>>

    // Get a single reminder by its ID (useful for scheduling notifications)
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): ReminderEntity?
}
