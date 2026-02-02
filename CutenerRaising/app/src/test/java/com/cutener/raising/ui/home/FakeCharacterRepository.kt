package com.cutener.raising.ui.home

import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCharacterRepository : CharacterRepository {
    private val _character = MutableStateFlow<Character?>(null)

    override fun getCharacter(): Flow<Character?> = _character

    override suspend fun saveCharacter(character: Character) {
        _character.value = character
    }

    override suspend fun deleteCharacter() {
        _character.value = null
    }
}
