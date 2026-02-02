package com.cutener.raising.domain.battle

import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.model.CharacterClass
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleEngineTest {

    @Test
    fun `simulateTurn calculates damage`() {
        val engine = BattleEngine()
        val attacker = Character(
            name = "Attacker",
            charClass = CharacterClass.WARRIOR,
            currentHp = 100, maxHp = 100,
            str = 20, intVal = 5, dex = 5
        )
        val defender = Character(
            name = "Defender",
            charClass = CharacterClass.WARRIOR,
            currentHp = 100, maxHp = 100,
            str = 10, intVal = 5, dex = 10
        )

        // Base damage: 20 - (10/2) = 15
        // Variation: -2 to +2 -> 13 to 17

        val log = engine.simulateTurn(attacker, defender)

        assertTrue(log.damage in 13..17)
    }
}
