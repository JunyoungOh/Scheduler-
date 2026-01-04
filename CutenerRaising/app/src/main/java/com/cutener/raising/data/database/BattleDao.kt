package com.cutener.raising.data.database

import androidx.room.*
import com.cutener.raising.data.model.BattleRecord
import com.cutener.raising.data.model.BattleResult
import kotlinx.coroutines.flow.Flow

@Dao
interface BattleDao {
    
    @Query("SELECT * FROM battle_history WHERE petId = :petId ORDER BY timestamp DESC")
    fun getBattleHistoryForPet(petId: Long): Flow<List<BattleRecord>>
    
    @Query("SELECT * FROM battle_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBattles(limit: Int = 10): Flow<List<BattleRecord>>
    
    @Query("SELECT * FROM battle_history ORDER BY timestamp DESC")
    suspend fun getAllBattlesOnce(): List<BattleRecord>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattle(battle: BattleRecord): Long
    
    @Query("SELECT COUNT(*) FROM battle_history WHERE petId = :petId AND result = :result")
    suspend fun countBattlesByResult(petId: Long, result: BattleResult): Int
    
    @Query("SELECT COUNT(*) FROM battle_history WHERE petId = :petId")
    suspend fun getTotalBattleCount(petId: Long): Int
    
    @Delete
    suspend fun deleteBattle(battle: BattleRecord)
    
    @Query("DELETE FROM battle_history WHERE petId = :petId")
    suspend fun deleteBattlesForPet(petId: Long)
}
