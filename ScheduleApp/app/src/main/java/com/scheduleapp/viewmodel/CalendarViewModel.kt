package com.scheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scheduleapp.data.model.*
import com.scheduleapp.data.repository.NoteDateLinkRepository
import com.scheduleapp.data.repository.NoteRepository
import com.scheduleapp.data.repository.PhotoRepository
import com.scheduleapp.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Calendar view type
 */
enum class CalendarViewType {
    MONTHLY,
    WEEKLY,
    DAILY
}

/**
 * UI State for Calendar screen
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val viewType: CalendarViewType = CalendarViewType.MONTHLY,
    val datesWithItems: Set<LocalDate> = emptySet(),
    val selectedDateItems: List<CalendarItem> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel for Calendar screen
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val noteRepository: NoteRepository,
    private val dateLinkRepository: NoteDateLinkRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    init {
        loadCalendarData()
        loadSelectedDateItems()
    }
    
    private fun loadCalendarData() {
        viewModelScope.launch {
            _uiState.value.let { state ->
                val startDate = state.currentMonth.atDay(1).minusDays(7)
                val endDate = state.currentMonth.atEndOfMonth().plusDays(7)
                
                // Collect dates with items
                combine(
                    scheduleRepository.getByDateRange(startDate, endDate),
                    dateLinkRepository.getByDateRange(startDate, endDate),
                    photoRepository.getByDateRange(startDate, endDate)
                ) { schedules, dateLinks, photos ->
                    val scheduleDates = schedules.map { it.date }.toSet()
                    val noteLinkDates = dateLinks.map { it.linkedDate }.toSet()
                    val photoDates = photos.map { it.date }.toSet()
                    scheduleDates + noteLinkDates + photoDates
                }.collect { datesWithItems ->
                    _uiState.update { it.copy(datesWithItems = datesWithItems, isLoading = false) }
                }
            }
        }
    }
    
    private fun loadSelectedDateItems() {
        viewModelScope.launch {
            _uiState.map { it.selectedDate }.distinctUntilChanged().collectLatest { date ->
                combine(
                    scheduleRepository.getByDate(date),
                    dateLinkRepository.getDisplayByDate(date),
                    photoRepository.getByDate(date)
                ) { schedules, dateLinksDisplay, photos ->
                    val items = mutableListOf<CalendarItem>()
                    
                    // Add schedules
                    items.addAll(schedules.map { CalendarItem.ScheduleItem(it) })
                    
                    // Add note date links (with note title from display data)
                    dateLinksDisplay.forEach { display ->
                        val dateLink = NoteDateLink(
                            id = display.dateLinkId,
                            noteId = display.noteId,
                            startIndex = 0,
                            endIndex = 0,
                            linkedDate = display.linkedDate,
                            linkedText = display.linkedText
                        )
                        items.add(CalendarItem.NoteLinkItem(dateLink, display.noteTitle))
                    }
                    
                    // Add photos
                    items.addAll(photos.map { CalendarItem.PhotoItem(it) })
                    
                    items
                }.collect { items ->
                    _uiState.update { it.copy(selectedDateItems = items) }
                }
            }
        }
    }
    
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }
    
    fun setViewType(viewType: CalendarViewType) {
        _uiState.update { it.copy(viewType = viewType) }
    }
    
    fun previousMonth() {
        _uiState.update { 
            val newMonth = it.currentMonth.minusMonths(1)
            it.copy(currentMonth = newMonth)
        }
        loadCalendarData()
    }
    
    fun nextMonth() {
        _uiState.update { 
            val newMonth = it.currentMonth.plusMonths(1)
            it.copy(currentMonth = newMonth)
        }
        loadCalendarData()
    }
    
    fun goToToday() {
        _uiState.update {
            it.copy(
                currentMonth = YearMonth.now(),
                selectedDate = LocalDate.now()
            )
        }
        loadCalendarData()
    }
    
    // Get calendar days for the current month view
    fun getCalendarDays(): List<LocalDate?> {
        val state = _uiState.value
        val firstDayOfMonth = state.currentMonth.atDay(1)
        val lastDayOfMonth = state.currentMonth.atEndOfMonth()
        
        // Get the first day to display (previous month days)
        val firstDayOfWeek = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        
        val days = mutableListOf<LocalDate?>()
        var currentDate = firstDayOfWeek
        
        // Generate 6 weeks of days
        repeat(42) {
            if (currentDate.month == state.currentMonth.month || 
                currentDate.isBefore(firstDayOfMonth) && currentDate.isAfter(firstDayOfWeek.minusDays(1)) ||
                currentDate.isAfter(lastDayOfMonth) && days.size < 42) {
                days.add(currentDate)
            } else if (days.isNotEmpty() && currentDate.month != state.currentMonth.month) {
                days.add(currentDate)
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return days.take(42)
    }
    
    // Get week days for weekly view
    fun getWeekDays(): List<LocalDate> {
        val selectedDate = _uiState.value.selectedDate
        val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        return (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }
}
