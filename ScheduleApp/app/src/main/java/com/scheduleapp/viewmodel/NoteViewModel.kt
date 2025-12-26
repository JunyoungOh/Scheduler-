package com.scheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scheduleapp.data.model.Note
import com.scheduleapp.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UI State for Note list screen
 */
data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

/**
 * UI State for Note detail/edit screen
 */
data class NoteDetailUiState(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val linkedDate: LocalDate? = null,
    val isPinned: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean get() = title.isNotBlank() || content.isNotBlank()
    val isEditing: Boolean get() = id != null
}

/**
 * ViewModel for Note screen
 */
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    private val _listState = MutableStateFlow(NoteListUiState())
    val listState: StateFlow<NoteListUiState> = _listState.asStateFlow()
    
    private val _detailState = MutableStateFlow(NoteDetailUiState())
    val detailState: StateFlow<NoteDetailUiState> = _detailState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    init {
        loadNotes()
    }
    
    private fun loadNotes() {
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                val notesFlow = if (query.isBlank()) {
                    noteRepository.getAll()
                } else {
                    noteRepository.search(query)
                }
                
                notesFlow.collect { notes ->
                    _listState.update { currentState ->
                        currentState.copy(
                            notes = notes,
                            searchQuery = query,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
    
    fun search(query: String) {
        _searchQuery.value = query
    }
    
    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            
            val note = noteRepository.getById(noteId)
            if (note != null) {
                _detailState.update {
                    NoteDetailUiState(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        linkedDate = note.linkedDate,
                        isPinned = note.isPinned,
                        isLoading = false
                    )
                }
            } else {
                _detailState.update { it.copy(isLoading = false, error = "노트를 찾을 수 없습니다") }
            }
        }
    }
    
    fun resetDetailState() {
        _detailState.value = NoteDetailUiState()
    }
    
    fun updateTitle(title: String) {
        _detailState.update { it.copy(title = title, error = null) }
    }
    
    fun updateContent(content: String) {
        _detailState.update { it.copy(content = content, error = null) }
    }
    
    fun updateLinkedDate(date: LocalDate?) {
        _detailState.update { it.copy(linkedDate = date) }
    }
    
    fun togglePinned() {
        val state = _detailState.value
        if (state.id != null) {
            viewModelScope.launch {
                noteRepository.togglePinned(state.id, !state.isPinned)
                _detailState.update { it.copy(isPinned = !state.isPinned) }
            }
        } else {
            _detailState.update { it.copy(isPinned = !state.isPinned) }
        }
    }
    
    fun save() {
        val state = _detailState.value
        
        if (!state.isValid) {
            _detailState.update { it.copy(error = "제목 또는 내용을 입력해주세요") }
            return
        }
        
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            
            try {
                val note = Note(
                    id = state.id ?: 0,
                    title = state.title.trim().ifBlank { "제목 없음" },
                    content = state.content.trim(),
                    linkedDate = state.linkedDate,
                    isPinned = state.isPinned
                )
                
                if (state.isEditing) {
                    noteRepository.update(note)
                } else {
                    noteRepository.insert(note)
                }
                
                _detailState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _detailState.update { it.copy(isLoading = false, error = "저장에 실패했습니다") }
            }
        }
    }
    
    fun delete() {
        val noteId = _detailState.value.id ?: return
        
        viewModelScope.launch {
            try {
                noteRepository.deleteById(noteId)
                _detailState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _detailState.update { it.copy(error = "삭제에 실패했습니다") }
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }
}
