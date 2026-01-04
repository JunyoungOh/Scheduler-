package com.cutener.raising.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cutener.raising.data.model.BattleRecord
import com.cutener.raising.data.model.Pet

@Database(
    entities = [Pet::class, BattleRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun battleDao(): BattleDao
}
