package com.scheduleapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.scheduleapp.data.model.Note
import com.scheduleapp.data.model.NoteDateLink
import com.scheduleapp.data.model.Photo
import com.scheduleapp.data.model.Priority
import com.scheduleapp.data.model.Schedule
import com.scheduleapp.data.repository.NoteDateLinkRepository
import com.scheduleapp.data.repository.NoteRepository
import com.scheduleapp.data.repository.PhotoRepository
import com.scheduleapp.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Backup data structure
 */
data class BackupData(
    val version: Int = 2,
    val exportedAt: String,
    val schedules: List<ScheduleBackup>,
    val notes: List<NoteBackup>,
    val noteDateLinks: List<NoteDateLinkBackup>,
    val photos: List<PhotoBackup>
)

data class ScheduleBackup(
    val id: Long,
    val title: String,
    val description: String?,
    val priority: Int,
    val date: String,
    val startTime: String?,
    val endTime: String?,
    val hasAlarm: Boolean,
    val alarmDateTime: String?,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class NoteBackup(
    val id: Long,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class NoteDateLinkBackup(
    val id: Long,
    val noteId: Long,
    val startIndex: Int,
    val endIndex: Int,
    val linkedDate: String,
    val linkedText: String,
    val createdAt: String
)

data class PhotoBackup(
    val id: Long,
    val uri: String,
    val memo: String?,
    val date: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * UI State for Setting screen
 */
data class SettingUiState(
    val isPasswordEnabled: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null,
    val appVersion: String = "1.0.0"
)

/**
 * ViewModel for Setting screen
 */
@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduleRepository: ScheduleRepository,
    private val noteRepository: NoteRepository,
    private val dateLinkRepository: NoteDateLinkRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()
    
    private val prefs = context.getSharedPreferences("schedule_app_prefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        val isPasswordEnabled = prefs.getBoolean("password_enabled", false)
        _uiState.update { it.copy(isPasswordEnabled = isPasswordEnabled) }
    }
    
    fun setPasswordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("password_enabled", enabled).apply()
        _uiState.update { it.copy(isPasswordEnabled = enabled) }
    }
    
    fun setPassword(password: String) {
        // In production, use proper encryption with EncryptedSharedPreferences
        prefs.edit().putString("app_password", password).apply()
    }
    
    fun verifyPassword(password: String): Boolean {
        val savedPassword = prefs.getString("app_password", null)
        return password == savedPassword
    }
    
    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null) }
            
            try {
                // Collect all data
                val schedules = scheduleRepository.getAllByDate().first()
                val notes = noteRepository.getAll().first()
                val photos = photoRepository.getAll().first()
                
                // Collect all date links for all notes
                val allDateLinks = mutableListOf<NoteDateLink>()
                notes.forEach { note ->
                    val links = dateLinkRepository.getByNoteIdSync(note.id)
                    allDateLinks.addAll(links)
                }
                
                // Convert to backup format
                val backupData = BackupData(
                    exportedAt = LocalDateTime.now().toString(),
                    schedules = schedules.map { it.toBackup() },
                    notes = notes.map { it.toBackup() },
                    noteDateLinks = allDateLinks.map { it.toBackup() },
                    photos = photos.map { it.toBackup() }
                )
                
                // Write to file
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(gson.toJson(backupData))
                    }
                }
                
                _uiState.update { it.copy(isExporting = false, exportSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isExporting = false, error = "내보내기에 실패했습니다: ${e.message}") 
                }
            }
        }
    }
    
    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null) }
            
            try {
                // Read file
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(inputStream.reader()).use { it.readText() }
                } ?: throw Exception("파일을 읽을 수 없습니다")
                
                // Parse backup data
                val backupData = gson.fromJson(jsonString, BackupData::class.java)
                
                // Clear existing data
                scheduleRepository.deleteAll()
                dateLinkRepository.deleteAll()
                noteRepository.deleteAll()
                photoRepository.deleteAll()
                
                // Map old note IDs to new IDs
                val noteIdMap = mutableMapOf<Long, Long>()
                
                // Import notes first
                backupData.notes.forEach { backup ->
                    val newId = noteRepository.insert(backup.toNote())
                    noteIdMap[backup.id] = newId
                }
                
                // Import note date links with updated note IDs
                backupData.noteDateLinks?.forEach { backup ->
                    val newNoteId = noteIdMap[backup.noteId]
                    if (newNoteId != null) {
                        dateLinkRepository.insert(backup.toNoteDateLink(newNoteId))
                    }
                }
                
                // Import schedules
                backupData.schedules.forEach { backup ->
                    scheduleRepository.insert(backup.toSchedule())
                }
                
                // Import photos
                backupData.photos.forEach { backup ->
                    photoRepository.insert(backup.toPhoto())
                }
                
                _uiState.update { it.copy(isImporting = false, importSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isImporting = false, error = "가져오기에 실패했습니다: ${e.message}") 
                }
            }
        }
    }
    
    fun clearSuccessFlags() {
        _uiState.update { it.copy(exportSuccess = false, importSuccess = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // Extension functions for conversion
    private fun Schedule.toBackup() = ScheduleBackup(
        id = id,
        title = title,
        description = description,
        priority = priority.value,
        date = date.toString(),
        startTime = startTime?.toString(),
        endTime = endTime?.toString(),
        hasAlarm = hasAlarm,
        alarmDateTime = alarmDateTime?.toString(),
        isCompleted = isCompleted,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
    
    private fun ScheduleBackup.toSchedule() = Schedule(
        id = 0, // Generate new ID
        title = title,
        description = description,
        priority = Priority.fromValue(priority),
        date = LocalDate.parse(date),
        startTime = startTime?.let { LocalTime.parse(it) },
        endTime = endTime?.let { LocalTime.parse(it) },
        hasAlarm = hasAlarm,
        alarmDateTime = alarmDateTime?.let { LocalDateTime.parse(it) },
        isCompleted = isCompleted,
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt)
    )
    
    private fun Note.toBackup() = NoteBackup(
        id = id,
        title = title,
        content = content,
        isPinned = isPinned,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
    
    private fun NoteBackup.toNote() = Note(
        id = 0,
        title = title,
        content = content,
        isPinned = isPinned,
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt)
    )
    
    private fun NoteDateLink.toBackup() = NoteDateLinkBackup(
        id = id,
        noteId = noteId,
        startIndex = startIndex,
        endIndex = endIndex,
        linkedDate = linkedDate.toString(),
        linkedText = linkedText,
        createdAt = createdAt.toString()
    )
    
    private fun NoteDateLinkBackup.toNoteDateLink(newNoteId: Long) = NoteDateLink(
        id = 0,
        noteId = newNoteId,
        startIndex = startIndex,
        endIndex = endIndex,
        linkedDate = LocalDate.parse(linkedDate),
        linkedText = linkedText,
        createdAt = LocalDateTime.parse(createdAt)
    )
    
    private fun Photo.toBackup() = PhotoBackup(
        id = id,
        uri = uri,
        memo = memo,
        date = date.toString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
    
    private fun PhotoBackup.toPhoto() = Photo(
        id = 0,
        uri = uri,
        memo = memo,
        date = LocalDate.parse(date),
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt)
    )
}
