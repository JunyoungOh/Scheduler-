package com.cutener.raising.data.repository

import com.cutener.raising.data.entity.toDomain
import com.cutener.raising.data.entity.toEntity
import com.cutener.raising.data.local.CharacterDao
import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val dao: CharacterDao
) : CharacterRepository {

    override fun getCharacter(): Flow<Character?> {
        return dao.getCharacter().map { it?.toDomain() }
    }

    override suspend fun saveCharacter(character: Character) {
        dao.insertCharacter(character.toEntity())
    }

    override suspend fun deleteCharacter() {
        dao.deleteAll()
    }
}
