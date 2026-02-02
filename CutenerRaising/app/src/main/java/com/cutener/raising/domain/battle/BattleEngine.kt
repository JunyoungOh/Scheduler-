package com.cutener.raising.domain.battle

import com.cutener.raising.domain.model.Character
import kotlin.random.Random

data class BattleLog(
    val message: String,
    val damage: Int
)

data class BattleResult(
    val winner: Character?,
    val logs: List<BattleLog>
)

class BattleEngine {

    fun simulateTurn(attacker: Character, defender: Character): BattleLog {
        // Simple logic: Damage = Attacker.Str - Defender.Dex/2 + Random Variation
        // Magic logic: if Int > Str, use Int as attack base

        val attackStat = maxOf(attacker.str, attacker.intVal)
        val defenseStat = defender.dex

        val baseDamage = (attackStat - (defenseStat / 2)).coerceAtLeast(1)
        val variation = Random.nextInt(-2, 3) // -2 to +2

        val finalDamage = (baseDamage + variation).coerceAtLeast(1)

        return BattleLog(
            message = "${attacker.name} attacks ${defender.name} for $finalDamage damage!",
            damage = finalDamage
        )
    }
}
