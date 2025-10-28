package com.example.placely.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placely.data.PlacelyRepository
import com.example.placely.data.entity.ReminderEntity
import com.example.placely.util.WorkManagerHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class ReminderViewModel(private val repository: PlacelyRepository) : ViewModel() {

    // --- State Flows for UI ---

    /**
     * All reminders from the repository, ordered by date/time
     */
    val reminders: Flow<List<ReminderEntity>> = repository.getAllReminders()

    /**
     * Only upcoming reminders (future events)
     */
    val upcomingReminders: Flow<List<ReminderEntity>> =
        repository.getAllReminders().map { reminders ->
            reminders.filter { it.dateTime >= System.currentTimeMillis() }
        }

    // --- Form State for Add/Edit Screen ---

    private val _reminderId = MutableStateFlow(0)
    val reminderId: StateFlow<Int> = _reminderId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow<String?>(null)
    val description: StateFlow<String?> = _description.asStateFlow()

    private val _dateTime = MutableStateFlow(System.currentTimeMillis())
    val dateTime: StateFlow<Long> = _dateTime.asStateFlow()

    private val _type = MutableStateFlow("Online Test")
    val type: StateFlow<String> = _type.asStateFlow()

    private val _alert = MutableStateFlow(3600000L) // Default: 1 hour before
    val alert: StateFlow<Long> = _alert.asStateFlow()

    // --- Public Methods ---

    /**
     * Load an existing reminder for editing
     */
    fun loadReminder(reminderId: Int) {
        viewModelScope.launch {
            repository.getReminderById(reminderId)?.let { reminder ->
                _reminderId.value = reminder.id
                _title.value = reminder.title
                _description.value = reminder.description
                _dateTime.value = reminder.dateTime
                _type.value = reminder.type
                _alert.value = reminder.notificationAlert
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
     * Update description field
     */
    fun setDescription(newDescription: String?) {
        _description.value = newDescription
    }

    /**
     * Update date and time as a complete timestamp
     */
    fun setDateTime(newDateTime: Long) {
        _dateTime.value = newDateTime
    }

    /**
     * Update only the date part while preserving the time
     */
    fun setDateTimeDate(newDateMillis: Long) {
        val currentDateTime = Instant.ofEpochMilli(_dateTime.value)
            .atZone(ZoneId.systemDefault())

        val newDate = Instant.ofEpochMilli(newDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val updatedDateTime = newDate
            .atTime(currentDateTime.toLocalTime())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        _dateTime.value = updatedDateTime
    }

    /**
     * Update only the time part while preserving the date
     */
    fun setDateTimeTime(hour: Int, minute: Int) {
        val currentDateTime = Instant.ofEpochMilli(_dateTime.value)
            .atZone(ZoneId.systemDefault())

        val updatedDateTime = currentDateTime.toLocalDate()
            .atTime(hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        _dateTime.value = updatedDateTime
    }

    /**
     * Update reminder type
     */
    fun setType(newType: String) {
        _type.value = newType
    }

    /**
     * Update notification alert time
     */
    fun setAlert(newAlert: Long) {
        _alert.value = newAlert
    }

    /**
     * Save the reminder (insert or update) and schedule notifications
     */
    fun saveReminder(context: Context) {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                id = if (_reminderId.value > 0) _reminderId.value else 0,
                title = _title.value.trim(),
                description = _description.value?.trim(),
                dateTime = _dateTime.value,
                type = _type.value,
                notificationAlert = _alert.value
            )

            val savedId = if (_reminderId.value > 0) {
                repository.updateReminder(reminder)
                _reminderId.value
            } else {
                repository.insertReminder(reminder).toInt()
            }

            // Schedule notifications using WorkManager
            val savedReminder = reminder.copy(id = savedId)
            WorkManagerHelper.scheduleReminderNotification(context, savedReminder)

            // Reset form state after saving
            resetFormState()
        }
    }

    /**
     * Delete the currently loaded reminder (used in edit screen)
     */
    fun deleteReminder(context: Context) {
        viewModelScope.launch {
            if (_reminderId.value > 0) {
                val reminder = ReminderEntity(
                    id = _reminderId.value,
                    title = _title.value,
                    description = _description.value,
                    dateTime = _dateTime.value,
                    type = _type.value,
                    notificationAlert = _alert.value
                )
                repository.deleteReminder(reminder)

                // Cancel scheduled notifications
                WorkManagerHelper.cancelReminderNotification(context, _reminderId.value)

                resetFormState()
            }
        }
    }

    /**
     * Delete a specific reminder
     */
    fun deleteReminder(reminder: ReminderEntity, context: Context) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            // Cancel scheduled notifications
            WorkManagerHelper.cancelReminderNotification(context, reminder.id)
        }
    }

    /**
     * Reset form state to initial values (for creating new reminders)
     */
    fun resetFormState() {
        _reminderId.value = 0
        _title.value = ""
        _description.value = null
        _dateTime.value = System.currentTimeMillis()
        _type.value = "Online Test"
        _alert.value = 3600000L // 1 hour
    }
}