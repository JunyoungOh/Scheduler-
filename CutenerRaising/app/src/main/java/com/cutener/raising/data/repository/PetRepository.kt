package com.cutener.raising.data.repository

import com.cutener.raising.data.database.PetDao
import com.cutener.raising.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor(
    private val petDao: PetDao
) {
    
    fun getActivePet(): Flow<Pet?> = petDao.getActivePet()
    
    suspend fun getActivePetOnce(): Pet? = petDao.getActivePetOnce()
    
    fun getPetById(id: Long): Flow<Pet?> = petDao.getPetById(id)
    
    suspend fun getPetByIdOnce(id: Long): Pet? = petDao.getPetByIdOnce(id)
    
    fun getAllPets(): Flow<List<Pet>> = petDao.getAllPets()
    
    suspend fun getAllPetsOnce(): List<Pet> = petDao.getAllPetsOnce()
    
    suspend fun createPet(name: String, type: PetType): Pet {
        // 기존 활성 펫 비활성화
        petDao.deactivateAllPets()
        
        // 새 펫 생성
        val newPet = PetDefaults.createNewPet(name, type)
        val id = petDao.insertPet(newPet)
        
        return newPet.copy(id = id)
    }
    
    suspend fun updatePet(pet: Pet) {
        petDao.updatePet(pet)
    }
    
    suspend fun deletePet(pet: Pet) {
        petDao.deletePet(pet)
    }
    
    suspend fun setActivePet(id: Long) {
        petDao.setActivePet(id)
    }
    
    suspend fun hasActivePet(): Boolean = petDao.hasActivePet()
    
    suspend fun getPetCount(): Int = petDao.getPetCount()
    
    /**
     * 액션 적용
     */
    suspend fun applyAction(pet: Pet, action: ActionType): Pet {
        val updatedPet = when (action) {
            ActionType.FEED -> ActionEffects.applyFeed(pet)
            ActionType.PLAY -> ActionEffects.applyPlay(pet)
            ActionType.CLEAN -> ActionEffects.applyClean(pet)
            ActionType.SLEEP -> ActionEffects.applySleep(pet)
            ActionType.WAKE -> ActionEffects.applyWake(pet)
            ActionType.TRAIN_STRENGTH -> ActionEffects.applyTrainStrength(pet)
            ActionType.TRAIN_DEFENSE -> ActionEffects.applyTrainDefense(pet)
            ActionType.TRAIN_SPEED -> ActionEffects.applyTrainSpeed(pet)
            ActionType.HEAL -> ActionEffects.applyHeal(pet)
            ActionType.BATTLE -> pet // 대결은 별도 처리
        }
        
        petDao.updatePet(updatedPet)
        return updatedPet
    }
    
    /**
     * 시간 경과 적용
     */
    suspend fun applyTimePassage(pet: Pet): Pet {
        val now = System.currentTimeMillis()
        val elapsedMinutes = (now - pet.lastUpdatedAt) / (1000 * 60)
        
        if (elapsedMinutes < 1) return pet
        
        var updatedPet = ActionEffects.applyTimePassage(pet, elapsedMinutes)
        
        // 진화 체크
        if (EvolutionChecker.canEvolve(updatedPet)) {
            updatedPet = EvolutionChecker.evolve(updatedPet)
        }
        
        petDao.updatePet(updatedPet)
        return updatedPet
    }
    
    /**
     * 대결 결과 적용
     */
    suspend fun applyBattleResult(pet: Pet, result: BattleResult): Pet {
        val newHistory = when (result) {
            BattleResult.WIN -> pet.careHistory.copy(
                battleWins = pet.careHistory.battleWins + 1
            )
            BattleResult.LOSE -> pet.careHistory.copy(
                battleLosses = pet.careHistory.battleLosses + 1
            )
            BattleResult.DRAW -> pet.careHistory
        }
        
        // 대결 후 피로도 증가
        val newCondition = pet.conditionStats.copy(
            fatigue = minOf(100, pet.conditionStats.fatigue + 15)
        )
        
        val updatedPet = pet.copy(
            careHistory = newHistory,
            conditionStats = newCondition,
            lastCaredAt = System.currentTimeMillis()
        )
        
        petDao.updatePet(updatedPet)
        return updatedPet
    }
}
