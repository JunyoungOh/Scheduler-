package com.scheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scheduleapp.data.model.Schedule
import com.scheduleapp.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sort type for schedule list
 */
enum class SortType {
    BY_DATE,
    BY_PRIORITY
}

/**
 * UI State for List screen
 */
data class ListUiState(
    val schedules: List<Schedule> = emptyList(),
    val sortType: SortType = SortType.BY_DATE,
    val isLoading: Boolean = true
)

/**
 * ViewModel for List screen
 */
@HiltViewModel
class ListViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    
    private val _sortType = MutableStateFlow(SortType.BY_DATE)
    
    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()
    
    init {
        loadSchedules()
    }
    
    private fun loadSchedules() {
        viewModelScope.launch {
            _sortType.collectLatest { sortType ->
                val schedulesFlow = when (sortType) {
                    SortType.BY_DATE -> scheduleRepository.getAllByDate()
                    SortType.BY_PRIORITY -> scheduleRepository.getAllByPriority()
                }
                
                schedulesFlow.collect { schedules ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            schedules = schedules,
                            sortType = sortType,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
    
    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
    }
    
    fun toggleCompleted(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.toggleCompleted(schedule.id, !schedule.isCompleted)
        }
    }
    
    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.delete(schedule)
        }
    }
}
