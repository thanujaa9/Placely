package com.example.placely.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.placely.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Get all tasks, ordered by completion status (uncompleted first) and then priority (High first)
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, deadline ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // Get only uncompleted tasks
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, deadline ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    // Query to get the count of completed tasks for the Progress Tracker feature
    @Query("SELECT COUNT(id) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?
}
