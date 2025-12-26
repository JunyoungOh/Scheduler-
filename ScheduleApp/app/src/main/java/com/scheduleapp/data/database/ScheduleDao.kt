package com.scheduleapp.data.database

import androidx.room.*
import com.scheduleapp.data.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Schedule entity
 */
@Dao
interface ScheduleDao {
    
    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<Schedule>)
    
    // Update
    @Update
    suspend fun update(schedule: Schedule)
    
    // Delete
    @Delete
    suspend fun delete(schedule: Schedule)
    
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
    
    // Query single
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Long): Schedule?
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Schedule?>
    
    // Query all - sorted by date and time (upcoming first)
    @Query("""
        SELECT * FROM schedules 
        ORDER BY date ASC, startTime ASC, createdAt DESC
    """)
    fun getAllByDate(): Flow<List<Schedule>>
    
    // Query all - sorted by priority (high first)
    @Query("""
        SELECT * FROM schedules 
        ORDER BY priority DESC, date ASC, startTime ASC
    """)
    fun getAllByPriority(): Flow<List<Schedule>>
    
    // Query by date
    @Query("SELECT * FROM schedules WHERE date = :date ORDER BY startTime ASC, priority DESC")
    fun getByDate(date: LocalDate): Flow<List<Schedule>>
    
    // Query by date range
    @Query("""
        SELECT * FROM schedules 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC, startTime ASC
    """)
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Schedule>>
    
    // Query upcoming (not completed, today or future)
    @Query("""
        SELECT * FROM schedules 
        WHERE isCompleted = 0 AND date >= :today 
        ORDER BY date ASC, startTime ASC
    """)
    fun getUpcoming(today: LocalDate = LocalDate.now()): Flow<List<Schedule>>
    
    // Query completed
    @Query("SELECT * FROM schedules WHERE isCompleted = 1 ORDER BY date DESC")
    fun getCompleted(): Flow<List<Schedule>>
    
    // Toggle completion status
    @Query("UPDATE schedules SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setCompleted(id: Long, isCompleted: Boolean, updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now())
    
    // Count schedules for a date
    @Query("SELECT COUNT(*) FROM schedules WHERE date = :date")
    suspend fun countByDate(date: LocalDate): Int
    
    // Get dates that have schedules in a month
    @Query("""
        SELECT DISTINCT date FROM schedules 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getDatesWithSchedules(startDate: LocalDate, endDate: LocalDate): List<LocalDate>
}
