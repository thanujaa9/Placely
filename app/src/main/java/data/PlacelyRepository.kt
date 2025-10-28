package com.example.placely.data

import com.example.placely.data.dao.NoteDao
import com.example.placely.data.dao.ReminderDao
import com.example.placely.data.dao.TaskDao
import com.example.placely.data.entity.NoteEntity
import com.example.placely.data.entity.ReminderEntity
import com.example.placely.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * The Repository acts as a single source of truth for the application's data.
 * It abstracts the data layer, making it easy to swap data sources (e.g., local Room DB vs. remote API)
 * without affecting the ViewModels.
 */
class PlacelyRepository(
    private val reminderDao: ReminderDao,
    private val noteDao: NoteDao,
    private val taskDao: TaskDao
) {
    // --- REMINDER OPERATIONS ---

    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun getReminderById(id: Int): ReminderEntity? = reminderDao.getReminderById(id)

    suspend fun insertReminder(reminder: ReminderEntity) = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)


    // --- NOTE OPERATIONS ---

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getPinnedNotes(): Flow<List<NoteEntity>> = noteDao.getPinnedNotes()

    suspend fun getNoteById(id: Int): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)


    // --- TASK OPERATIONS ---

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getAllPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    suspend fun getTaskById(id: Int): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updatedTask)
    }
    // Add these methods to PlacelyRepository.kt

    // --- NOTE OPERATIONS (missing methods) ---
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    // --- TASK OPERATIONS (missing method) ---
    fun getCompletedTaskCount(): Flow<Int> = taskDao.getCompletedTaskCount()
}
