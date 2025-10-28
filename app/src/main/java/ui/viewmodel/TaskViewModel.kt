package com.example.placely.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placely.data.PlacelyRepository
import com.example.placely.data.entity.TaskEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class TaskViewModel(private val repository: PlacelyRepository) : ViewModel() {

    // --- State Flows for UI ---

    /**
     * All tasks from the repository, ordered by completion status and priority
     */
    val tasks: Flow<List<TaskEntity>> = repository.getAllTasks()

    /**
     * Only pending (uncompleted) tasks
     */
    val pendingTasks: Flow<List<TaskEntity>> = repository.getAllPendingTasks()

    // --- Form State for Add/Edit Screen ---

    private val _taskId = MutableStateFlow(0)
    val taskId: StateFlow<Int> = _taskId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _deadline = MutableStateFlow<Long?>(null)
    val deadline: StateFlow<Long?> = _deadline.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _priority = MutableStateFlow("Low")
    val priority: StateFlow<String> = _priority.asStateFlow()

    // --- Filter State ---

    private val _showCompletedTasks = MutableStateFlow(true)
    val showCompletedTasks: StateFlow<Boolean> = _showCompletedTasks.asStateFlow()

    /**
     * Filtered tasks based on completion filter
     */
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        tasks,
        showCompletedTasks
    ) { taskList, showCompleted ->
        if (showCompleted) {
            taskList
        } else {
            taskList.filter { !it.isCompleted }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Public Methods ---

    /**
     * Load an existing task for editing
     */
    fun loadTask(taskId: Int) {
        viewModelScope.launch {
            repository.getTaskById(taskId)?.let { task ->
                _taskId.value = task.id
                _title.value = task.title
                _deadline.value = task.deadline
                _isCompleted.value = task.isCompleted
                _priority.value = task.priority
            }
        }
    }

    /**
     * Update title field
     */
    fun setTitle(newTitle: String) {
        _title.value = newTitle
    }

    /**
     * Update deadline
     */
    fun setDeadline(newDeadline: Long?) {
        _deadline.value = newDeadline
    }

    /**
     * Update only the date part of deadline while preserving the time
     */
    fun setDeadlineDate(newDateMillis: Long) {
        val currentDeadline = _deadline.value

        val updatedDeadline = if (currentDeadline != null) {
            // Preserve existing time
            val currentDateTime = Instant.ofEpochMilli(currentDeadline)
                .atZone(ZoneId.systemDefault())

            val newDate = Instant.ofEpochMilli(newDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            newDate
                .atTime(currentDateTime.toLocalTime())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } else {
            // No existing deadline, set to start of selected day
            val newDate = Instant.ofEpochMilli(newDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            newDate
                .atTime(9, 0) // Default to 9:00 AM
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        _deadline.value = updatedDeadline
    }

    /**
     * Update only the time part of deadline while preserving the date
     */
    fun setDeadlineTime(hour: Int, minute: Int) {
        val currentDeadline = _deadline.value ?: System.currentTimeMillis()

        val currentDateTime = Instant.ofEpochMilli(currentDeadline)
            .atZone(ZoneId.systemDefault())

        val updatedDeadline = currentDateTime.toLocalDate()
            .atTime(hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        _deadline.value = updatedDeadline
    }

    /**
     * Clear deadline
     */
    fun clearDeadline() {
        _deadline.value = null
    }

    /**
     * Toggle completion status
     */
    fun toggleCompletion() {
        _isCompleted.value = !_isCompleted.value
    }

    /**
     * Set completion status
     */
    fun setCompleted(completed: Boolean) {
        _isCompleted.value = completed
    }

    /**
     * Update priority
     */
    fun setPriority(newPriority: String) {
        _priority.value = newPriority
    }

    /**
     * Toggle filter for showing completed tasks
     */
    fun toggleShowCompleted() {
        _showCompletedTasks.value = !_showCompletedTasks.value
    }

    /**
     * Save the task (insert or update)
     */
    fun saveTask(context: Context) {
        viewModelScope.launch {
            val task = TaskEntity(
                id = if (_taskId.value > 0) _taskId.value else 0,
                title = _title.value.trim(),
                deadline = _deadline.value,
                isCompleted = _isCompleted.value,
                priority = _priority.value
            )

            if (_taskId.value > 0) {
                repository.updateTask(task)
            } else {
                repository.insertTask(task)
            }

            // Reset form state after saving
            resetFormState()
        }
    }

    /**
     * Delete the currently loaded task (used in edit screen)
     */
    fun deleteCurrentTask(context: Context) {
        viewModelScope.launch {
            if (_taskId.value > 0) {
                val task = TaskEntity(
                    id = _taskId.value,
                    title = _title.value,
                    deadline = _deadline.value,
                    isCompleted = _isCompleted.value,
                    priority = _priority.value
                )
                repository.deleteTask(task)
                resetFormState()
            }
        }
    }

    /**
     * Delete a specific task
     */
    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    /**
     * Toggle completion status of a specific task (uses repository method)
     */
    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    /**
     * Update priority of a specific task
     */
    fun updateTaskPriority(task: TaskEntity, newPriority: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(priority = newPriority))
        }
    }

    /**
     * Reset form state to initial values (for creating new tasks)
     */
    fun resetFormState() {
        _taskId.value = 0
        _title.value = ""
        _deadline.value = null
        _isCompleted.value = false
        _priority.value = "Low"
    }

    /**
     * Clear all completed tasks
     */
    fun clearCompletedTasks() {
        viewModelScope.launch {
            tasks.firstOrNull()
                ?.filter { it.isCompleted }
                ?.forEach { repository.deleteTask(it) }
        }
    }
}