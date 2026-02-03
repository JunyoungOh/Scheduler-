package com.cutener.raising.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cutener.raising.domain.model.CharacterClass
import com.cutener.raising.domain.model.Character

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val classType: CharacterClass,
    val level: Int,
    val currentHp: Int,
    val maxHp: Int,
    val currentEnergy: Int,
    val maxEnergy: Int,
    val str: Int,
    val intVal: Int,
    val dex: Int,
    val exp: Int,
    val maxExp: Int
)

fun CharacterEntity.toDomain() = Character(
    id = id,
    name = name,
    charClass = classType,
    level = level,
    currentHp = currentHp,
    maxHp = maxHp,
    currentEnergy = currentEnergy,
    maxEnergy = maxEnergy,
    str = str,
    intVal = intVal,
    dex = dex,
    exp = exp,
    maxExp = maxExp
)

fun Character.toEntity() = CharacterEntity(
    id = id,
    name = name,
    classType = charClass,
    level = level,
    currentHp = currentHp,
    maxHp = maxHp,
    currentEnergy = currentEnergy,
    maxEnergy = maxEnergy,
    str = str,
    intVal = intVal,
    dex = dex,
    exp = exp,
    maxExp = maxExp
)
