package com.cutener.raising.data.database

import androidx.room.*
import com.cutener.raising.data.model.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    
    @Query("SELECT * FROM pets WHERE isActive = 1 LIMIT 1")
    fun getActivePet(): Flow<Pet?>
    
    @Query("SELECT * FROM pets WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePetOnce(): Pet?
    
    @Query("SELECT * FROM pets WHERE id = :id")
    fun getPetById(id: Long): Flow<Pet?>
    
    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getPetByIdOnce(id: Long): Pet?
    
    @Query("SELECT * FROM pets ORDER BY createdAt DESC")
    fun getAllPets(): Flow<List<Pet>>
    
    @Query("SELECT * FROM pets ORDER BY createdAt DESC")
    suspend fun getAllPetsOnce(): List<Pet>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: Pet): Long
    
    @Update
    suspend fun updatePet(pet: Pet)
    
    @Delete
    suspend fun deletePet(pet: Pet)
    
    @Query("UPDATE pets SET isActive = 0")
    suspend fun deactivateAllPets()
    
    @Query("UPDATE pets SET isActive = 1 WHERE id = :id")
    suspend fun activatePet(id: Long)
    
    @Transaction
    suspend fun setActivePet(id: Long) {
        deactivateAllPets()
        activatePet(id)
    }
    
    @Query("SELECT COUNT(*) FROM pets")
    suspend fun getPetCount(): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM pets WHERE isActive = 1)")
    suspend fun hasActivePet(): Boolean
}
