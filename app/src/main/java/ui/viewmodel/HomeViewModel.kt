package com.example.placely.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placely.data.PlacelyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit

class HomeViewModel(private val repository: PlacelyRepository) : ViewModel() {

    // --- Data Flows from Repository ---

    /**
     * Upcoming reminders: Filters reminders to only show those scheduled in the future.
     * Only takes the top 3 for the dashboard.
     */
    val upcomingReminders = repository.getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Pending tasks: Filters tasks to show incomplete high-priority items.
     * Only takes the top 5 for the dashboard.
     */
    val pendingTasks = repository.getAllPendingTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Motivational Content (Mocked for now) ---

    /**
     * A simple list of quotes to cycle through or display.
     * In a real app, this might come from a remote source or resource file.
     */
    private val quotes = listOf(
        "Success is not final, failure is not fatal: it is the courage to continue that counts.",
        "The only way to do great work is to love what you do.",
        "The best time to plant a tree was 20 years ago. The second best time is now.",
        "Your work is going to fill a large part of your life, and the only way to be truly satisfied is to do what you believe is great work."
    )

    private val tips = listOf(
        "Dedicate 10 minutes every morning to review your high-priority list.",
        "Break down large preparation goals into smaller, manageable chunks.",
        "Don't study late. A well-rested mind performs better in interviews.",
        "Practice the STAR method for behavioral interview questions."
    )

    /**
     * Exposes a fixed quote for the Home Screen UI.
     * In a production environment, you might update this periodically.
     */
    val quote: StateFlow<String> = flowOf(quotes.random())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = quotes.first()
        )

    val motivationalTip: StateFlow<String> = flowOf(tips.random())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = tips.first()
        )
}
