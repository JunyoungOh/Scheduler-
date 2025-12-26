package com.scheduleapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Note entity for storing notes
 * Notes can have optional date associations for calendar integration
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Note content
    val title: String,
    val content: String,
    
    // Optional date association for calendar integration
    // This allows notes to appear on specific dates in Calendar
    val linkedDate: LocalDate? = null,
    
    // Metadata
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    // For pinning important notes
    val isPinned: Boolean = false
)

/**
 * Represents a section of note content that has a date
 * Used for parsing note content for calendar integration
 */
data class NoteDateSection(
    val noteId: Long,
    val startIndex: Int,
    val endIndex: Int,
    val text: String,
    val linkedDate: LocalDate
)

/**
 * Extension to check if note has calendar integration
 */
fun Note.hasCalendarLink(): Boolean = linkedDate != null
