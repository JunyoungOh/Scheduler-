package com.cutener.raising.domain.repository

import com.cutener.raising.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacter(): Flow<Character?>
    suspend fun saveCharacter(character: Character)
    suspend fun deleteCharacter()
}
