package com.scheduleapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.scheduleapp.data.model.Note
import com.scheduleapp.data.model.Photo
import com.scheduleapp.data.model.Priority
import com.scheduleapp.data.model.Schedule
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
    val version: Int = 1,
    val exportedAt: String,
    val schedules: List<ScheduleBackup>,
    val notes: List<NoteBackup>,
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
    val linkedDate: String?,
    val isPinned: Boolean,
    val createdAt: String,
    val updatedAt: String
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
                
                // Convert to backup format
                val backupData = BackupData(
                    exportedAt = LocalDateTime.now().toString(),
                    schedules = schedules.map { it.toBackup() },
                    notes = notes.map { it.toBackup() },
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
                noteRepository.deleteAll()
                photoRepository.deleteAll()
                
                // Import schedules
                backupData.schedules.forEach { backup ->
                    scheduleRepository.insert(backup.toSchedule())
                }
                
                // Import notes
                backupData.notes.forEach { backup ->
                    noteRepository.insert(backup.toNote())
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
        linkedDate = linkedDate?.toString(),
        isPinned = isPinned,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
    
    private fun NoteBackup.toNote() = Note(
        id = 0,
        title = title,
        content = content,
        linkedDate = linkedDate?.let { LocalDate.parse(it) },
        isPinned = isPinned,
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt)
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
