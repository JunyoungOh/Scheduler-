package com.cutener.raising.domain.model

data class Character(
    val id: Int = 0,
    val name: String,
    val charClass: CharacterClass,
    val level: Int = 1,
    val currentHp: Int,
    val maxHp: Int,
    val currentEnergy: Int = 100,
    val maxEnergy: Int = 100,
    val str: Int,
    val intVal: Int,
    val dex: Int,
    val exp: Int = 0,
    val maxExp: Int = 100
)
