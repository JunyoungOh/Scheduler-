package com.scheduleapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scheduleapp.data.model.Photo
import com.scheduleapp.data.model.PhotoGroup
import com.scheduleapp.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * UI State for Photo list screen
 */
data class PhotoListUiState(
    val photoGroups: List<PhotoGroup> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false
)

/**
 * UI State for adding a photo
 */
data class PhotoAddState(
    val uri: Uri? = null,
    val memo: String = "",
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Photo screen
 */
@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {
    
    private val _listState = MutableStateFlow(PhotoListUiState())
    val listState: StateFlow<PhotoListUiState> = _listState.asStateFlow()
    
    private val _addState = MutableStateFlow(PhotoAddState())
    val addState: StateFlow<PhotoAddState> = _addState.asStateFlow()
    
    private val _selectedPhoto = MutableStateFlow<Photo?>(null)
    val selectedPhoto: StateFlow<Photo?> = _selectedPhoto.asStateFlow()
    
    init {
        loadPhotos()
    }
    
    private fun loadPhotos() {
        viewModelScope.launch {
            photoRepository.getAllGrouped().collect { groups ->
                _listState.update { 
                    it.copy(photoGroups = groups, isLoading = false) 
                }
            }
        }
    }
    
    fun showAddDialog() {
        _addState.value = PhotoAddState()
        _listState.update { it.copy(showAddDialog = true) }
    }
    
    fun hideAddDialog() {
        _listState.update { it.copy(showAddDialog = false) }
    }
    
    fun setPhotoUri(uri: Uri) {
        _addState.update { it.copy(uri = uri) }
    }
    
    fun updateMemo(memo: String) {
        _addState.update { it.copy(memo = memo) }
    }
    
    fun updateDate(date: LocalDate) {
        _addState.update { it.copy(date = date) }
    }
    
    fun savePhoto() {
        val state = _addState.value
        
        if (state.uri == null) {
            _addState.update { it.copy(error = "사진을 선택해주세요") }
            return
        }
        
        viewModelScope.launch {
            _addState.update { it.copy(isLoading = true) }
            
            try {
                val photo = Photo(
                    uri = state.uri.toString(),
                    memo = state.memo.trim().ifBlank { null },
                    date = state.date
                )
                
                photoRepository.insert(photo)
                
                _addState.update { it.copy(isLoading = false, isSaved = true) }
                hideAddDialog()
            } catch (e: Exception) {
                _addState.update { it.copy(isLoading = false, error = "저장에 실패했습니다") }
            }
        }
    }
    
    fun loadPhoto(photoId: Long) {
        viewModelScope.launch {
            _selectedPhoto.value = photoRepository.getById(photoId)
        }
    }
    
    fun updatePhotoMemo(photoId: Long, memo: String) {
        viewModelScope.launch {
            photoRepository.updateMemo(photoId, memo.trim().ifBlank { null })
            _selectedPhoto.value = photoRepository.getById(photoId)
        }
    }
    
    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            photoRepository.delete(photo)
        }
    }
    
    fun deletePhotoById(photoId: Long) {
        viewModelScope.launch {
            photoRepository.deleteById(photoId)
            _selectedPhoto.value = null
        }
    }
}
