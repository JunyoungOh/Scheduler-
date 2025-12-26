package com.scheduleapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Photo entity for storing photo records with dates
 * Photos are linked to dates for calendar integration
 */
@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Photo URI (content:// or file://)
    val uri: String,
    
    // Optional memo/description
    val memo: String? = null,
    
    // Required date for calendar integration
    val date: LocalDate,
    
    // Metadata
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Photo group for displaying photos grouped by date
 */
data class PhotoGroup(
    val date: LocalDate,
    val photos: List<Photo>
)
