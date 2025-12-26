package com.scheduleapp.data.repository

import com.scheduleapp.data.database.ScheduleDao
import com.scheduleapp.data.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Schedule data operations
 */
@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    
    // Get all schedules sorted by date
    fun getAllByDate(): Flow<List<Schedule>> = scheduleDao.getAllByDate()
    
    // Get all schedules sorted by priority
    fun getAllByPriority(): Flow<List<Schedule>> = scheduleDao.getAllByPriority()
    
    // Get schedule by ID
    suspend fun getById(id: Long): Schedule? = scheduleDao.getById(id)
    
    // Get schedule by ID as Flow
    fun getByIdFlow(id: Long): Flow<Schedule?> = scheduleDao.getByIdFlow(id)
    
    // Get schedules for a specific date
    fun getByDate(date: LocalDate): Flow<List<Schedule>> = scheduleDao.getByDate(date)
    
    // Get schedules in a date range
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Schedule>> =
        scheduleDao.getByDateRange(startDate, endDate)
    
    // Get upcoming schedules
    fun getUpcoming(): Flow<List<Schedule>> = scheduleDao.getUpcoming()
    
    // Get completed schedules
    fun getCompleted(): Flow<List<Schedule>> = scheduleDao.getCompleted()
    
    // Insert schedule
    suspend fun insert(schedule: Schedule): Long = scheduleDao.insert(schedule)
    
    // Update schedule
    suspend fun update(schedule: Schedule) {
        scheduleDao.update(schedule.copy(updatedAt = LocalDateTime.now()))
    }
    
    // Delete schedule
    suspend fun delete(schedule: Schedule) = scheduleDao.delete(schedule)
    
    // Delete by ID
    suspend fun deleteById(id: Long) = scheduleDao.deleteById(id)
    
    // Toggle completion
    suspend fun toggleCompleted(id: Long, isCompleted: Boolean) {
        scheduleDao.setCompleted(id, isCompleted)
    }
    
    // Get count for a date
    suspend fun countByDate(date: LocalDate): Int = scheduleDao.countByDate(date)
    
    // Get dates with schedules
    suspend fun getDatesWithSchedules(startDate: LocalDate, endDate: LocalDate): List<LocalDate> =
        scheduleDao.getDatesWithSchedules(startDate, endDate)
    
    // Delete all
    suspend fun deleteAll() = scheduleDao.deleteAll()
    
    // Insert all (for backup restore)
    suspend fun insertAll(schedules: List<Schedule>) = scheduleDao.insertAll(schedules)
}
