package com.scheduleapp.data.database

import androidx.room.*
import com.scheduleapp.data.model.NoteDateLink
import com.scheduleapp.data.model.NoteDateLinkDisplay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for NoteDateLink entity
 */
@Dao
interface NoteDateLinkDao {
    
    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dateLink: NoteDateLink): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dateLinks: List<NoteDateLink>)
    
    // Update
    @Update
    suspend fun update(dateLink: NoteDateLink)
    
    // Delete
    @Delete
    suspend fun delete(dateLink: NoteDateLink)
    
    @Query("DELETE FROM note_date_links WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM note_date_links WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Long)
    
    @Query("DELETE FROM note_date_links")
    suspend fun deleteAll()
    
    // Query by ID
    @Query("SELECT * FROM note_date_links WHERE id = :id")
    suspend fun getById(id: Long): NoteDateLink?
    
    // Query by note ID
    @Query("SELECT * FROM note_date_links WHERE noteId = :noteId ORDER BY startIndex ASC")
    fun getByNoteId(noteId: Long): Flow<List<NoteDateLink>>
    
    @Query("SELECT * FROM note_date_links WHERE noteId = :noteId ORDER BY startIndex ASC")
    suspend fun getByNoteIdSync(noteId: Long): List<NoteDateLink>
    
    // Query by date (for calendar)
    @Query("SELECT * FROM note_date_links WHERE linkedDate = :date ORDER BY createdAt ASC")
    fun getByDate(date: LocalDate): Flow<List<NoteDateLink>>
    
    // Query by date range (for calendar)
    @Query("""
        SELECT * FROM note_date_links 
        WHERE linkedDate BETWEEN :startDate AND :endDate 
        ORDER BY linkedDate ASC, createdAt ASC
    """)
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<NoteDateLink>>
    
    // Query with note info for calendar display
    @Query("""
        SELECT 
            ndl.id as dateLinkId,
            ndl.noteId,
            n.title as noteTitle,
            ndl.linkedText,
            ndl.linkedDate
        FROM note_date_links ndl
        INNER JOIN notes n ON ndl.noteId = n.id
        WHERE ndl.linkedDate = :date
        ORDER BY ndl.createdAt ASC
    """)
    fun getDisplayByDate(date: LocalDate): Flow<List<NoteDateLinkDisplay>>
    
    // Get dates that have note links in a date range
    @Query("""
        SELECT DISTINCT linkedDate FROM note_date_links 
        WHERE linkedDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getDatesWithLinks(startDate: LocalDate, endDate: LocalDate): List<LocalDate>
    
    // Count links for a date
    @Query("SELECT COUNT(*) FROM note_date_links WHERE linkedDate = :date")
    suspend fun countByDate(date: LocalDate): Int
    
    // Check if a text range overlaps with existing links
    @Query("""
        SELECT * FROM note_date_links 
        WHERE noteId = :noteId 
          AND ((startIndex <= :endIndex AND endIndex >= :startIndex))
    """)
    suspend fun getOverlappingLinks(noteId: Long, startIndex: Int, endIndex: Int): List<NoteDateLink>
}
