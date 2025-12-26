package com.scheduleapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entity representing a date-linked section within a note.
 * A single note can have multiple date links, each linking a specific
 * text range to a specific date.
 * 
 * Example:
 * Note content: "회의록: 다음주 금요일까지 보고서 제출, 월말까지 프로젝트 완료"
 * DateLink 1: "다음주 금요일까지 보고서 제출" → 2024-01-12
 * DateLink 2: "월말까지 프로젝트 완료" → 2024-01-31
 */
@Entity(
    tableName = "note_date_links",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["noteId"]),
        Index(value = ["linkedDate"])
    ]
)
data class NoteDateLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Reference to parent note
    val noteId: Long,
    
    // Text range within the note content
    val startIndex: Int,
    val endIndex: Int,
    
    // The linked date for calendar integration
    val linkedDate: LocalDate,
    
    // Cached text for display in calendar (extracted from note content)
    val linkedText: String,
    
    // Metadata
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Data class combining Note with its date links
 */
data class NoteWithDateLinks(
    val note: Note,
    val dateLinks: List<NoteDateLink>
) {
    val hasDateLinks: Boolean get() = dateLinks.isNotEmpty()
}

/**
 * Data class for displaying date link in calendar
 */
data class NoteDateLinkDisplay(
    val dateLinkId: Long,
    val noteId: Long,
    val noteTitle: String,
    val linkedText: String,
    val linkedDate: LocalDate
)
