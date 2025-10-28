package com.example.placely.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placely.data.PlacelyRepository
import com.example.placely.data.entity.NoteEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: PlacelyRepository) : ViewModel() {

    // --- State Flows for UI ---

    /**
     * All notes from the repository, ordered by pin status and timestamp
     */
    val notes: Flow<List<NoteEntity>> = repository.getAllNotes()

    /**
     * Search query state
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Filtered notes based on search query
     */
    val filteredNotes: StateFlow<List<NoteEntity>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllNotes()
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Form State for Add/Edit Screen ---

    private val _noteId = MutableStateFlow(0)
    val noteId: StateFlow<Int> = _noteId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    // --- Public Methods ---

    /**
     * Load an existing note for editing
     */
    fun loadNote(noteId: Int) {
        viewModelScope.launch {
            repository.getAllNotes().firstOrNull()?.find { it.id == noteId }?.let { note ->
                _noteId.value = note.id
                _title.value = note.title
                _content.value = note.content
                _isPinned.value = note.isPinned
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
     * Update content field
     */
    fun setContent(newContent: String) {
        _content.value = newContent
    }

    /**
     * Toggle pin status
     */
    fun togglePin() {
        _isPinned.value = !_isPinned.value
    }

    /**
     * Update search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Save the note (insert or update)
     */
    fun saveNote(context: Context) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = if (_noteId.value > 0) _noteId.value else 0,
                title = _title.value.trim(),
                content = _content.value.trim(),
                isPinned = _isPinned.value,
                timestamp = System.currentTimeMillis()
            )

            if (_noteId.value > 0) {
                repository.updateNote(note)
            } else {
                repository.insertNote(note)
            }

            // Reset form state after saving
            resetFormState()
        }
    }

    /**
     * Delete a note
     */
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    /**
     * Delete the currently loaded note (used in edit screen)
     */
    fun deleteCurrentNote(context: Context) {
        viewModelScope.launch {
            if (_noteId.value > 0) {
                val note = NoteEntity(
                    id = _noteId.value,
                    title = _title.value,
                    content = _content.value,
                    isPinned = _isPinned.value
                )
                repository.deleteNote(note)
                resetFormState()
            }
        }
    }

    /**
     * Toggle pin status for a specific note
     */
    fun toggleNotePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    /**
     * Reset form state to initial values (for creating new notes)
     */
    fun resetFormState() {
        _noteId.value = 0
        _title.value = ""
        _content.value = ""
        _isPinned.value = false
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
}