package com.scheduleapp.data.database

import androidx.room.*
import com.scheduleapp.data.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Note entity
 */
@Dao
interface NoteDao {
    
    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)
    
    // Update
    @Update
    suspend fun update(note: Note)
    
    // Delete
    @Delete
    suspend fun delete(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM notes")
    suspend fun deleteAll()
    
    // Query single
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?
    
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Note?>
    
    // Query all - pinned first, then by updated date
    @Query("""
        SELECT * FROM notes 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getAll(): Flow<List<Note>>
    
    // Query by linked date (for calendar integration)
    @Query("SELECT * FROM notes WHERE linkedDate = :date ORDER BY updatedAt DESC")
    fun getByLinkedDate(date: LocalDate): Flow<List<Note>>
    
    // Query notes with linked dates (for calendar)
    @Query("SELECT * FROM notes WHERE linkedDate IS NOT NULL ORDER BY linkedDate ASC")
    fun getNotesWithDates(): Flow<List<Note>>
    
    // Search notes
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun search(query: String): Flow<List<Note>>
    
    // Toggle pin status
    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean, updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now())
    
    // Update linked date
    @Query("UPDATE notes SET linkedDate = :date, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setLinkedDate(id: Long, date: LocalDate?, updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now())
    
    // Count notes for a date
    @Query("SELECT COUNT(*) FROM notes WHERE linkedDate = :date")
    suspend fun countByDate(date: LocalDate): Int
    
    // Get dates that have notes
    @Query("""
        SELECT DISTINCT linkedDate FROM notes 
        WHERE linkedDate IS NOT NULL 
          AND linkedDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getDatesWithNotes(startDate: LocalDate, endDate: LocalDate): List<LocalDate>
}
