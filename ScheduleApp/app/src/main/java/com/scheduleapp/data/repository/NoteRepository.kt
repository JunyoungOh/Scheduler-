package com.scheduleapp.data.repository

import com.scheduleapp.data.database.NoteDao
import com.scheduleapp.data.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Note data operations.
 * 
 * Note: Date linking is now handled through NoteDateLinkRepository,
 * not directly in Note.
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
    
    // Delete all
    suspend fun deleteAll() = noteDao.deleteAll()
    
    // Insert all (for backup restore)
    suspend fun insertAll(notes: List<Note>) = noteDao.insertAll(notes)
}
