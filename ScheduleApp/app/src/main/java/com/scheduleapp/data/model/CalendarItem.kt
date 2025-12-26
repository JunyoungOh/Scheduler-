package com.scheduleapp.data.model

import java.time.LocalDate

/**
 * Sealed class representing different types of items
 * that can appear on a calendar date
 */
sealed class CalendarItem {
    abstract val id: Long
    abstract val date: LocalDate
    abstract val displayTitle: String
    
    data class ScheduleItem(
        val schedule: Schedule
    ) : CalendarItem() {
        override val id: Long get() = schedule.id
        override val date: LocalDate get() = schedule.date
        override val displayTitle: String get() = schedule.title
    }
    
    data class NoteItem(
        val note: Note
    ) : CalendarItem() {
        override val id: Long get() = note.id
        override val date: LocalDate get() = note.linkedDate ?: LocalDate.now()
        override val displayTitle: String get() = note.title
    }
    
    data class PhotoItem(
        val photo: Photo
    ) : CalendarItem() {
        override val id: Long get() = photo.id
        override val date: LocalDate get() = photo.date
        override val displayTitle: String get() = photo.memo ?: "사진"
    }
}

/**
 * Data class for daily summary in calendar
 */
data class DailySummary(
    val date: LocalDate,
    val scheduleCount: Int = 0,
    val noteCount: Int = 0,
    val photoCount: Int = 0,
    val items: List<CalendarItem> = emptyList()
) {
    val hasItems: Boolean get() = scheduleCount > 0 || noteCount > 0 || photoCount > 0
    val totalCount: Int get() = scheduleCount + noteCount + photoCount
}
