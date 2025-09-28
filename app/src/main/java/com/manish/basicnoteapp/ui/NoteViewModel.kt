package com.manish.basicnoteapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manish.basicnoteapp.data.Note
import com.manish.basicnoteapp.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect { notesList ->
                _notes.value = notesList
            }
        }
    }

    fun loadNoteById(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentNote.value = repository.getNoteById(id)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentNote = _currentNote.value
                if (currentNote != null) {
                    // Update existing note
                    val updatedNote = currentNote.copy(
                        title = title,
                        content = content,
                        updatedAt = Date()
                    )
                    repository.updateNote(updatedNote)
                } else {
                    // Create new note
                    val newNote = Note(
                        title = title,
                        content = content,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                    repository.insertNote(newNote)
                }
                _currentNote.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun clearCurrentNote() {
        _currentNote.value = null
    }
}
