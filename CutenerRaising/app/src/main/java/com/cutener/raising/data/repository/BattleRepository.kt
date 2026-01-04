package com.cutener.raising.data.repository

import com.cutener.raising.data.database.BattleDao
import com.cutener.raising.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleRepository @Inject constructor(
    private val battleDao: BattleDao
) {
    
    fun getBattleHistoryForPet(petId: Long): Flow<List<BattleRecord>> =
        battleDao.getBattleHistoryForPet(petId)
    
    fun getRecentBattles(limit: Int = 10): Flow<List<BattleRecord>> =
        battleDao.getRecentBattles(limit)
    
    suspend fun recordBattle(
        pet: Pet,
        opponent: BattlePetData,
        result: BattleResult,
        turns: List<BattleTurn>
    ): BattleRecord {
        val battleLog = turns.joinToString("\n") { turn ->
            "${turn.turnNumber}. ${turn.message}"
        }
        
        val record = BattleRecord(
            petId = pet.id,
            petName = pet.name,
            opponentName = opponent.name,
            opponentType = opponent.type,
            opponentStage = opponent.stage,
            myStrength = pet.battleStats.strength,
            myDefense = pet.battleStats.defense,
            mySpeed = pet.battleStats.speed,
            myHp = pet.battleStats.maxHp,
            opponentStrength = opponent.strength,
            opponentDefense = opponent.defense,
            opponentSpeed = opponent.speed,
            opponentHp = opponent.maxHp,
            result = result,
            battleLog = battleLog
        )
        
        val id = battleDao.insertBattle(record)
        return record.copy(id = id)
    }
    
    suspend fun getWinCount(petId: Long): Int =
        battleDao.countBattlesByResult(petId, BattleResult.WIN)
    
    suspend fun getLoseCount(petId: Long): Int =
        battleDao.countBattlesByResult(petId, BattleResult.LOSE)
    
    suspend fun getTotalBattleCount(petId: Long): Int =
        battleDao.getTotalBattleCount(petId)
    
    suspend fun deleteBattlesForPet(petId: Long) {
        battleDao.deleteBattlesForPet(petId)
    }
}
