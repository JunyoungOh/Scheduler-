package com.scheduleapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Priority levels for schedules
 */
enum class Priority(val value: Int, val displayName: String) {
    HIGH(3, "높음"),
    MEDIUM(2, "보통"),
    LOW(1, "낮음");
    
    companion object {
        fun fromValue(value: Int): Priority = entries.find { it.value == value } ?: MEDIUM
    }
}

/**
 * Schedule entity for storing schedule items
 * Used in List and Calendar tabs
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Required field
    val title: String,
    
    // Optional fields
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    
    // Date and time
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    
    // Alarm settings
    val hasAlarm: Boolean = false,
    val alarmDateTime: LocalDateTime? = null,
    
    // Metadata
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Extension function to check if schedule is overdue
 */
fun Schedule.isOverdue(): Boolean {
    val now = LocalDateTime.now()
    val scheduleDateTime = if (startTime != null) {
        LocalDateTime.of(date, startTime)
    } else {
        LocalDateTime.of(date, LocalTime.MAX)
    }
    return !isCompleted && scheduleDateTime.isBefore(now)
}

/**
 * Extension function to check if schedule is today
 */
fun Schedule.isToday(): Boolean = date == LocalDate.now()

/**
 * Extension function to check if schedule is upcoming
 */
fun Schedule.isUpcoming(): Boolean = date.isAfter(LocalDate.now())
