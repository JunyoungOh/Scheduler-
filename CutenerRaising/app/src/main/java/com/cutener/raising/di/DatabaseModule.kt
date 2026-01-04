package com.cutener.raising.di

import android.content.Context
import androidx.room.Room
import com.cutener.raising.data.database.AppDatabase
import com.cutener.raising.data.database.BattleDao
import com.cutener.raising.data.database.PetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cutener_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun providePetDao(database: AppDatabase): PetDao {
        return database.petDao()
    }
    
    @Provides
    fun provideBattleDao(database: AppDatabase): BattleDao {
        return database.battleDao()
    }
}
