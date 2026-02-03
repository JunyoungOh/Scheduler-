package com.cutener.raising.domain.battle

import com.cutener.raising.domain.model.Character
import kotlin.random.Random

sealed class BattleEvent {
    abstract val message: String

    data class Hit(
        override val message: String,
        val damage: Int,
        val isCritical: Boolean
    ) : BattleEvent()

    data class Miss(
        override val message: String
    ) : BattleEvent()
}

data class BattleResult(
    val winner: Character?,
    val events: List<BattleEvent>
)

class BattleEngine {

    fun simulateTurn(attacker: Character, defender: Character): BattleEvent {
        // Miss Logic: Based on Dex difference
        // If Defender Dex is significantly higher, chance to miss. Base 85% hit chance.
        // If Attacker has more Dex, hit chance increases.
        val hitChance = 85 + (attacker.dex - defender.dex)
        val roll = Random.nextInt(0, 100)

        // Always 5% chance to miss at least, unless hitChance is super high (clamped logic?)
        // Let's keep it simple.
        if (roll >= hitChance) {
             return BattleEvent.Miss("${attacker.name} attacks but misses!")
        }

        // Damage Logic
        val attackStat = maxOf(attacker.str, attacker.intVal)
        val defenseStat = defender.dex // Dex acts as defense/evasion hybrid

        var baseDamage = (attackStat - (defenseStat / 3)).coerceAtLeast(1)
        val variation = Random.nextInt(-2, 3) // -2 to +2

        // Critical Logic
        val isCrit = Random.nextInt(0, 100) < 15 // 15% Crit chance
        if (isCrit) {
            baseDamage = (baseDamage * 1.5).toInt()
        }

        val finalDamage = (baseDamage + variation).coerceAtLeast(1)

        val msg = if (isCrit)
            "CRITICAL! ${attacker.name} deals $finalDamage damage to ${defender.name}!"
        else
            "${attacker.name} attacks ${defender.name} for $finalDamage damage!"

        return BattleEvent.Hit(
            message = msg,
            damage = finalDamage,
            isCritical = isCrit
        )
    }
}
