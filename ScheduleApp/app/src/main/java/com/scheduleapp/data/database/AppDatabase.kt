package com.scheduleapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scheduleapp.data.model.Note
import com.scheduleapp.data.model.NoteDateLink
import com.scheduleapp.data.model.Photo
import com.scheduleapp.data.model.Schedule

/**
 * Main Room database for the Schedule app
 */
@Database(
    entities = [
        Schedule::class,
        Note::class,
        NoteDateLink::class,
        Photo::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun scheduleDao(): ScheduleDao
    abstract fun noteDao(): NoteDao
    abstract fun noteDateLinkDao(): NoteDateLinkDao
    abstract fun photoDao(): PhotoDao
    
    companion object {
        private const val DATABASE_NAME = "schedule_app_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
