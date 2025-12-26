package com.scheduleapp.data.database

import androidx.room.*
import com.scheduleapp.data.model.Photo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Photo entity
 */
@Dao
interface PhotoDao {
    
    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<Photo>)
    
    // Update
    @Update
    suspend fun update(photo: Photo)
    
    // Delete
    @Delete
    suspend fun delete(photo: Photo)
    
    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM photos")
    suspend fun deleteAll()
    
    // Query single
    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getById(id: Long): Photo?
    
    @Query("SELECT * FROM photos WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Photo?>
    
    // Query all - by date descending (most recent first)
    @Query("SELECT * FROM photos ORDER BY date DESC, createdAt DESC")
    fun getAll(): Flow<List<Photo>>
    
    // Query by date
    @Query("SELECT * FROM photos WHERE date = :date ORDER BY createdAt DESC")
    fun getByDate(date: LocalDate): Flow<List<Photo>>
    
    // Query by date range
    @Query("""
        SELECT * FROM photos 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Photo>>
    
    // Update memo
    @Query("UPDATE photos SET memo = :memo, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMemo(id: Long, memo: String?, updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now())
    
    // Count photos for a date
    @Query("SELECT COUNT(*) FROM photos WHERE date = :date")
    suspend fun countByDate(date: LocalDate): Int
    
    // Get dates that have photos
    @Query("""
        SELECT DISTINCT date FROM photos 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getDatesWithPhotos(startDate: LocalDate, endDate: LocalDate): List<LocalDate>
    
    // Get distinct dates (for grouping)
    @Query("SELECT DISTINCT date FROM photos ORDER BY date DESC")
    fun getDistinctDates(): Flow<List<LocalDate>>
}
