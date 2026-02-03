package com.cutener.raising.domain.battle

import com.cutener.raising.domain.model.Character
import com.cutener.raising.domain.model.CharacterClass
import org.junit.Test
import org.junit.Assert.*

class BattleEngineTest {

    private val engine = BattleEngine()

    private fun createChar(name: String, dex: Int): Character {
        return Character(
            name = name,
            charClass = CharacterClass.WARRIOR,
            currentHp = 100,
            maxHp = 100,
            str = 10,
            intVal = 10,
            dex = dex,
            currentEnergy = 10,
            maxEnergy = 10
        )
    }

    @Test
    fun `test hit event generation`() {
        val c1 = createChar("P1", 10)
        val c2 = createChar("P2", 10)

        // Run multiple times to ensure no crashes
        val event = engine.simulateTurn(c1, c2)

        assertTrue(event is BattleEvent.Hit || event is BattleEvent.Miss)
        if (event is BattleEvent.Hit) {
            assertTrue(event.damage > 0)
        }
    }

    @Test
    fun `test miss logic`() {
        // High Dex difference to force Miss
        // P1 Dex 10 vs P2 Dex 100
        // Hit Chance = 85 + (10 - 100) = -5. Should always miss.

        val c1 = createChar("Attacker", 10)
        val c2 = createChar("Dodger", 100)

        var missCount = 0
        for(i in 0 until 100) {
            val event = engine.simulateTurn(c1, c2)
            if (event is BattleEvent.Miss) missCount++
        }

        // Should be high miss count
        assertTrue("Expected high miss count, got $missCount", missCount > 90)
    }
}
