package com.scheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scheduleapp.data.model.Priority
import com.scheduleapp.data.model.Schedule
import com.scheduleapp.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * UI State for Schedule form
 */
data class ScheduleFormState(
    val id: Long? = null,
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val hasAlarm: Boolean = false,
    val alarmDateTime: LocalDateTime? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean get() = title.isNotBlank()
    val isEditing: Boolean get() = id != null
}

/**
 * ViewModel for Schedule add/edit
 */
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    
    private val _formState = MutableStateFlow(ScheduleFormState())
    val formState: StateFlow<ScheduleFormState> = _formState.asStateFlow()
    
    private val _schedule = MutableStateFlow<Schedule?>(null)
    val schedule: StateFlow<Schedule?> = _schedule.asStateFlow()
    
    fun loadSchedule(scheduleId: Long) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            
            val schedule = scheduleRepository.getById(scheduleId)
            if (schedule != null) {
                _schedule.value = schedule
                _formState.update {
                    ScheduleFormState(
                        id = schedule.id,
                        title = schedule.title,
                        description = schedule.description ?: "",
                        date = schedule.date,
                        startTime = schedule.startTime,
                        endTime = schedule.endTime,
                        priority = schedule.priority,
                        hasAlarm = schedule.hasAlarm,
                        alarmDateTime = schedule.alarmDateTime,
                        isLoading = false
                    )
                }
            } else {
                _formState.update { it.copy(isLoading = false, error = "일정을 찾을 수 없습니다") }
            }
        }
    }
    
    fun setInitialDate(dateString: String?) {
        if (dateString != null) {
            try {
                val date = LocalDate.parse(dateString)
                _formState.update { it.copy(date = date) }
            } catch (e: Exception) {
                // Ignore parsing error
            }
        }
    }
    
    fun updateTitle(title: String) {
        _formState.update { it.copy(title = title, error = null) }
    }
    
    fun updateDescription(description: String) {
        _formState.update { it.copy(description = description) }
    }
    
    fun updateDate(date: LocalDate) {
        _formState.update { it.copy(date = date) }
    }
    
    fun updateStartTime(time: LocalTime?) {
        _formState.update { it.copy(startTime = time) }
    }
    
    fun updateEndTime(time: LocalTime?) {
        _formState.update { it.copy(endTime = time) }
    }
    
    fun updatePriority(priority: Priority) {
        _formState.update { it.copy(priority = priority) }
    }
    
    fun updateHasAlarm(hasAlarm: Boolean) {
        _formState.update { it.copy(hasAlarm = hasAlarm) }
    }
    
    fun updateAlarmDateTime(dateTime: LocalDateTime?) {
        _formState.update { it.copy(alarmDateTime = dateTime) }
    }
    
    fun save() {
        val state = _formState.value
        
        if (state.title.isBlank()) {
            _formState.update { it.copy(error = "제목을 입력해주세요") }
            return
        }
        
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            
            try {
                val schedule = Schedule(
                    id = state.id ?: 0,
                    title = state.title.trim(),
                    description = state.description.trim().ifBlank { null },
                    date = state.date,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    priority = state.priority,
                    hasAlarm = state.hasAlarm,
                    alarmDateTime = state.alarmDateTime
                )
                
                if (state.isEditing) {
                    scheduleRepository.update(schedule)
                } else {
                    scheduleRepository.insert(schedule)
                }
                
                _formState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _formState.update { it.copy(isLoading = false, error = "저장에 실패했습니다") }
            }
        }
    }
    
    fun delete() {
        val scheduleId = _formState.value.id ?: return
        
        viewModelScope.launch {
            try {
                scheduleRepository.deleteById(scheduleId)
                _formState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _formState.update { it.copy(error = "삭제에 실패했습니다") }
            }
        }
    }
    
    fun toggleCompleted() {
        val currentSchedule = _schedule.value ?: return
        
        viewModelScope.launch {
            scheduleRepository.toggleCompleted(currentSchedule.id, !currentSchedule.isCompleted)
            _schedule.value = scheduleRepository.getById(currentSchedule.id)
        }
    }
}
