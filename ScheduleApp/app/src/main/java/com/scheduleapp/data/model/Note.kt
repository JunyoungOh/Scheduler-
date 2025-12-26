package com.scheduleapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Note entity for storing notes.
 * 
 * Date linking is now handled at the sentence/text-range level
 * through NoteDateLink entity. A single note can have multiple
 * text sections linked to different dates.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Note content
    val title: String,
    val content: String,
    
    // Metadata
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    // For pinning important notes
    val isPinned: Boolean = false
)
