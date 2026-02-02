package com.cutener.raising.domain.model

enum class CharacterClass(
    val title: String,
    val baseHp: Int,
    val baseStr: Int,
    val baseInt: Int,
    val baseDex: Int
) {
    WARRIOR("Warrior", 100, 10, 2, 5),
    MAGE("Mage", 60, 2, 12, 5),
    PALADIN("Paladin", 120, 8, 5, 3),
    DARK_KNIGHT("Dark Knight", 90, 12, 4, 6),
    ROGUE("Rogue", 70, 6, 3, 12),
    ARCHER("Archer", 75, 7, 3, 10);
}
