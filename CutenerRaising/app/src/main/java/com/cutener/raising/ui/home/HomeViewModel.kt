package com.cutener.raising.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.model.CharacterAnimState
import com.cutener.raising.domain.model.CharacterClass
import com.cutener.raising.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CharacterRepository
) : ViewModel() {

    val characterState: StateFlow<Character?> = repository.getCharacter()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _animState = MutableStateFlow(CharacterAnimState.IDLE)
    val animState = _animState.asStateFlow()

    fun createCharacter(name: String, charClass: CharacterClass) {
        viewModelScope.launch {
            val newCharacter = Character(
                name = name,
                charClass = charClass,
                currentHp = charClass.baseHp,
                maxHp = charClass.baseHp,
                str = charClass.baseStr,
                intVal = charClass.baseInt,
                dex = charClass.baseDex
            )
            repository.saveCharacter(newCharacter)
        }
    }

    fun train() {
        val current = characterState.value ?: return
        if (current.currentEnergy < 10) return

        val stats = listOf("str", "int", "dex")
        val boostedStat = stats.random()

        // Simple logic: +1 to a random stat, -10 energy, +10 exp
        var newStr = current.str
        var newInt = current.intVal
        var newDex = current.dex

        when(boostedStat) {
            "str" -> newStr++
            "int" -> newInt++
            "dex" -> newDex++
        }

        val newExp = current.exp + 10
        val (finalLevel, finalExp, finalMaxExp) = checkLevelUp(current.level, newExp, current.maxExp)

        val updated = current.copy(
            str = newStr,
            intVal = newInt,
            dex = newDex,
            currentEnergy = current.currentEnergy - 10,
            level = finalLevel,
            exp = finalExp,
            maxExp = finalMaxExp
        )
        updateCharacter(updated)

        viewModelScope.launch {
            _animState.value = CharacterAnimState.TRAINING
            delay(2000)
            _animState.value = CharacterAnimState.IDLE
        }
    }

    fun feed() {
        val current = characterState.value ?: return
        // Restore HP, maybe cost energy? Or restore energy?
        // Let's say Feed restores HP and Energy but maybe has cooldown (omitted for now)
        val newHp = (current.currentHp + 20).coerceAtMost(current.maxHp)
        val newEnergy = (current.currentEnergy + 20).coerceAtMost(current.maxEnergy)

        updateCharacter(current.copy(currentHp = newHp, currentEnergy = newEnergy))

        viewModelScope.launch {
            _animState.value = CharacterAnimState.EATING
            delay(2000)
            _animState.value = CharacterAnimState.IDLE
        }
    }

    fun rest() {
        val current = characterState.value ?: return
        // Full restore Energy
        val newEnergy = current.maxEnergy
        updateCharacter(current.copy(currentEnergy = newEnergy))

        viewModelScope.launch {
            _animState.value = CharacterAnimState.SLEEPING
            delay(3000)
            _animState.value = CharacterAnimState.IDLE
        }
    }

    fun deleteCharacter() {
        viewModelScope.launch {
            repository.deleteCharacter()
        }
    }

    private fun updateCharacter(character: Character) {
        viewModelScope.launch {
            repository.saveCharacter(character)
        }
    }

    private fun checkLevelUp(level: Int, exp: Int, maxExp: Int): Triple<Int, Int, Int> {
        if (exp >= maxExp) {
            return Triple(level + 1, exp - maxExp, (maxExp * 1.2).toInt())
        }
        return Triple(level, exp, maxExp)
    }
}
