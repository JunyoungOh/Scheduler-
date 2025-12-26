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
    
    /**
     * Represents a date-linked section from a note.
     * Links to a specific text range within a note.
     */
    data class NoteLinkItem(
        val dateLink: NoteDateLink,
        val noteTitle: String
    ) : CalendarItem() {
        override val id: Long get() = dateLink.id
        override val date: LocalDate get() = dateLink.linkedDate
        override val displayTitle: String get() = dateLink.linkedText
        
        val noteId: Long get() = dateLink.noteId
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
    val noteLinkCount: Int = 0,
    val photoCount: Int = 0,
    val items: List<CalendarItem> = emptyList()
) {
    val hasItems: Boolean get() = scheduleCount > 0 || noteLinkCount > 0 || photoCount > 0
    val totalCount: Int get() = scheduleCount + noteLinkCount + photoCount
}
