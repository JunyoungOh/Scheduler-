package com.scheduleapp.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scheduleapp.data.model.Note
import com.scheduleapp.data.model.NoteDateLink
import com.scheduleapp.data.repository.NoteDateLinkRepository
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
    val dateLinks: List<NoteDateLink> = emptyList(),
    val isPinned: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null,
    // For text selection and date linking
    val selectedTextRange: TextRange? = null,
    val showDateLinkDialog: Boolean = false
) {
    val isValid: Boolean get() = title.isNotBlank() || content.isNotBlank()
    val isEditing: Boolean get() = id != null
    val hasSelection: Boolean get() = selectedTextRange != null && 
        selectedTextRange.length > 0
}

/**
 * ViewModel for Note screen
 */
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val dateLinkRepository: NoteDateLinkRepository
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
                val dateLinks = dateLinkRepository.getByNoteIdSync(noteId)
                _detailState.update {
                    NoteDetailUiState(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        dateLinks = dateLinks,
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
        // When content changes, we need to adjust date link positions
        val oldContent = _detailState.value.content
        val newContent = content
        
        if (oldContent != newContent) {
            val adjustedLinks = adjustDateLinksForContentChange(
                _detailState.value.dateLinks,
                oldContent,
                newContent
            )
            _detailState.update { 
                it.copy(
                    content = content, 
                    dateLinks = adjustedLinks,
                    error = null
                ) 
            }
        }
    }
    
    /**
     * Adjust date link positions when content changes.
     * This is a simplified approach - in production, you'd want
     * more sophisticated text diffing.
     */
    private fun adjustDateLinksForContentChange(
        links: List<NoteDateLink>,
        oldContent: String,
        newContent: String
    ): List<NoteDateLink> {
        // For now, invalidate links if content length changed significantly
        // A more sophisticated approach would track exact text changes
        return links.filter { link ->
            link.endIndex <= newContent.length &&
            newContent.substring(link.startIndex, link.endIndex) == link.linkedText
        }
    }
    
    fun setTextSelection(range: TextRange) {
        _detailState.update { it.copy(selectedTextRange = range) }
    }
    
    fun clearTextSelection() {
        _detailState.update { it.copy(selectedTextRange = null) }
    }
    
    fun showDateLinkDialog() {
        _detailState.update { it.copy(showDateLinkDialog = true) }
    }
    
    fun hideDateLinkDialog() {
        _detailState.update { it.copy(showDateLinkDialog = false) }
    }
    
    /**
     * Add a date link for the currently selected text
     */
    fun addDateLink(date: LocalDate) {
        val state = _detailState.value
        val selection = state.selectedTextRange ?: return
        
        if (selection.length <= 0) return
        
        val noteId = state.id
        val selectedText = state.content.substring(selection.start, selection.end)
        
        val newLink = NoteDateLink(
            noteId = noteId ?: 0,
            startIndex = selection.start,
            endIndex = selection.end,
            linkedDate = date,
            linkedText = selectedText
        )
        
        // Add to local state (will be persisted on save)
        _detailState.update { 
            it.copy(
                dateLinks = it.dateLinks + newLink,
                selectedTextRange = null,
                showDateLinkDialog = false
            )
        }
    }
    
    /**
     * Remove a date link
     */
    fun removeDateLink(dateLink: NoteDateLink) {
        viewModelScope.launch {
            if (dateLink.id != 0L) {
                dateLinkRepository.deleteById(dateLink.id)
            }
            _detailState.update { 
                it.copy(dateLinks = it.dateLinks.filter { it.id != dateLink.id })
            }
        }
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
                    isPinned = state.isPinned
                )
                
                val noteId = if (state.isEditing) {
                    noteRepository.update(note)
                    state.id!!
                } else {
                    noteRepository.insert(note)
                }
                
                // Save date links
                // First, delete existing links for this note
                dateLinkRepository.deleteByNoteId(noteId)
                
                // Then insert new/updated links
                val linksToSave = state.dateLinks.map { link ->
                    link.copy(noteId = noteId, id = 0) // Reset ID for new insertion
                }
                if (linksToSave.isNotEmpty()) {
                    dateLinkRepository.insertAll(linksToSave)
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
                // Date links will be deleted automatically due to CASCADE
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
    
    /**
     * Get date links count for a note (for list display)
     */
    suspend fun getDateLinksCount(noteId: Long): Int {
        return dateLinkRepository.getByNoteIdSync(noteId).size
    }
}
