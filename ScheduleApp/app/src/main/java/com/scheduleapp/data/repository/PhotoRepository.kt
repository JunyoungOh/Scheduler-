package com.scheduleapp.data.repository

import com.scheduleapp.data.database.PhotoDao
import com.scheduleapp.data.model.Photo
import com.scheduleapp.data.model.PhotoGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Photo data operations
 */
@Singleton
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao
) {
    
    // Get all photos
    fun getAll(): Flow<List<Photo>> = photoDao.getAll()
    
    // Get photos grouped by date
    fun getAllGrouped(): Flow<List<PhotoGroup>> = photoDao.getAll().map { photos ->
        photos.groupBy { it.date }
            .map { (date, photoList) -> PhotoGroup(date, photoList) }
            .sortedByDescending { it.date }
    }
    
    // Get photo by ID
    suspend fun getById(id: Long): Photo? = photoDao.getById(id)
    
    // Get photo by ID as Flow
    fun getByIdFlow(id: Long): Flow<Photo?> = photoDao.getByIdFlow(id)
    
    // Get photos by date
    fun getByDate(date: LocalDate): Flow<List<Photo>> = photoDao.getByDate(date)
    
    // Get photos in a date range
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Photo>> =
        photoDao.getByDateRange(startDate, endDate)
    
    // Insert photo
    suspend fun insert(photo: Photo): Long = photoDao.insert(photo)
    
    // Update photo
    suspend fun update(photo: Photo) {
        photoDao.update(photo.copy(updatedAt = LocalDateTime.now()))
    }
    
    // Delete photo
    suspend fun delete(photo: Photo) = photoDao.delete(photo)
    
    // Delete by ID
    suspend fun deleteById(id: Long) = photoDao.deleteById(id)
    
    // Update memo
    suspend fun updateMemo(id: Long, memo: String?) {
        photoDao.updateMemo(id, memo)
    }
    
    // Get count for a date
    suspend fun countByDate(date: LocalDate): Int = photoDao.countByDate(date)
    
    // Get dates with photos
    suspend fun getDatesWithPhotos(startDate: LocalDate, endDate: LocalDate): List<LocalDate> =
        photoDao.getDatesWithPhotos(startDate, endDate)
    
    // Get distinct dates
    fun getDistinctDates(): Flow<List<LocalDate>> = photoDao.getDistinctDates()
    
    // Delete all
    suspend fun deleteAll() = photoDao.deleteAll()
    
    // Insert all (for backup restore)
    suspend fun insertAll(photos: List<Photo>) = photoDao.insertAll(photos)
}
