package com.scheduleapp.data.repository

import com.scheduleapp.data.database.NoteDateLinkDao
import com.scheduleapp.data.model.NoteDateLink
import com.scheduleapp.data.model.NoteDateLinkDisplay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for NoteDateLink data operations
 */
@Singleton
class NoteDateLinkRepository @Inject constructor(
    private val noteDateLinkDao: NoteDateLinkDao
) {
    
    // Insert a new date link
    suspend fun insert(dateLink: NoteDateLink): Long = noteDateLinkDao.insert(dateLink)
    
    // Insert multiple date links
    suspend fun insertAll(dateLinks: List<NoteDateLink>) = noteDateLinkDao.insertAll(dateLinks)
    
    // Update a date link
    suspend fun update(dateLink: NoteDateLink) = noteDateLinkDao.update(dateLink)
    
    // Delete a date link
    suspend fun delete(dateLink: NoteDateLink) = noteDateLinkDao.delete(dateLink)
    
    // Delete by ID
    suspend fun deleteById(id: Long) = noteDateLinkDao.deleteById(id)
    
    // Delete all links for a note
    suspend fun deleteByNoteId(noteId: Long) = noteDateLinkDao.deleteByNoteId(noteId)
    
    // Get date link by ID
    suspend fun getById(id: Long): NoteDateLink? = noteDateLinkDao.getById(id)
    
    // Get all date links for a note
    fun getByNoteId(noteId: Long): Flow<List<NoteDateLink>> = noteDateLinkDao.getByNoteId(noteId)
    
    // Get all date links for a note (synchronous)
    suspend fun getByNoteIdSync(noteId: Long): List<NoteDateLink> = noteDateLinkDao.getByNoteIdSync(noteId)
    
    // Get date links for a specific date (for calendar)
    fun getByDate(date: LocalDate): Flow<List<NoteDateLink>> = noteDateLinkDao.getByDate(date)
    
    // Get date links in a date range
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<NoteDateLink>> =
        noteDateLinkDao.getByDateRange(startDate, endDate)
    
    // Get display info for calendar
    fun getDisplayByDate(date: LocalDate): Flow<List<NoteDateLinkDisplay>> = 
        noteDateLinkDao.getDisplayByDate(date)
    
    // Get dates that have links in a range
    suspend fun getDatesWithLinks(startDate: LocalDate, endDate: LocalDate): List<LocalDate> =
        noteDateLinkDao.getDatesWithLinks(startDate, endDate)
    
    // Count links for a date
    suspend fun countByDate(date: LocalDate): Int = noteDateLinkDao.countByDate(date)
    
    // Check for overlapping links
    suspend fun getOverlappingLinks(noteId: Long, startIndex: Int, endIndex: Int): List<NoteDateLink> =
        noteDateLinkDao.getOverlappingLinks(noteId, startIndex, endIndex)
    
    // Delete all
    suspend fun deleteAll() = noteDateLinkDao.deleteAll()
}
