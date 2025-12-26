package com.scheduleapp.data.repository

import com.scheduleapp.data.database.NoteDao
import com.scheduleapp.data.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Note data operations
 */
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    
    // Get all notes
    fun getAll(): Flow<List<Note>> = noteDao.getAll()
    
    // Get note by ID
    suspend fun getById(id: Long): Note? = noteDao.getById(id)
    
    // Get note by ID as Flow
    fun getByIdFlow(id: Long): Flow<Note?> = noteDao.getByIdFlow(id)
    
    // Get notes by linked date
    fun getByLinkedDate(date: LocalDate): Flow<List<Note>> = noteDao.getByLinkedDate(date)
    
    // Get notes with dates (for calendar)
    fun getNotesWithDates(): Flow<List<Note>> = noteDao.getNotesWithDates()
    
    // Search notes
    fun search(query: String): Flow<List<Note>> = noteDao.search(query)
    
    // Insert note
    suspend fun insert(note: Note): Long = noteDao.insert(note)
    
    // Update note
    suspend fun update(note: Note) {
        noteDao.update(note.copy(updatedAt = LocalDateTime.now()))
    }
    
    // Delete note
    suspend fun delete(note: Note) = noteDao.delete(note)
    
    // Delete by ID
    suspend fun deleteById(id: Long) = noteDao.deleteById(id)
    
    // Toggle pin
    suspend fun togglePinned(id: Long, isPinned: Boolean) {
        noteDao.setPinned(id, isPinned)
    }
    
    // Set linked date
    suspend fun setLinkedDate(id: Long, date: LocalDate?) {
        noteDao.setLinkedDate(id, date)
    }
    
    // Get count for a date
    suspend fun countByDate(date: LocalDate): Int = noteDao.countByDate(date)
    
    // Get dates with notes
    suspend fun getDatesWithNotes(startDate: LocalDate, endDate: LocalDate): List<LocalDate> =
        noteDao.getDatesWithNotes(startDate, endDate)
    
    // Delete all
    suspend fun deleteAll() = noteDao.deleteAll()
    
    // Insert all (for backup restore)
    suspend fun insertAll(notes: List<Note>) = noteDao.insertAll(notes)
}
