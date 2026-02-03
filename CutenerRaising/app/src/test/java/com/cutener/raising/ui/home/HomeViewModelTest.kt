package com.cutener.raising.ui.home

import com.cutener.raising.domain.model.CharacterClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: FakeCharacterRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCharacterRepository()
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createCharacter creates a character correctly`() = runTest {
        viewModel.createCharacter("TestHero", CharacterClass.WARRIOR)
        testDispatcher.scheduler.advanceUntilIdle()

        val character = viewModel.characterState.value
        assertNotNull(character)
        assertEquals("TestHero", character?.name)
        assertEquals(CharacterClass.WARRIOR, character?.charClass)
        assertEquals(CharacterClass.WARRIOR.baseHp, character?.currentHp)
    }

    @Test
    fun `train increases stats and decreases energy`() = runTest {
        viewModel.createCharacter("TestHero", CharacterClass.WARRIOR)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialCharacter = viewModel.characterState.value!!
        val initialStr = initialCharacter.str
        val initialInt = initialCharacter.intVal
        val initialDex = initialCharacter.dex
        val initialEnergy = initialCharacter.currentEnergy

        viewModel.train()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedCharacter = viewModel.characterState.value!!

        // Energy should decrease by 10
        assertEquals(initialEnergy - 10, updatedCharacter.currentEnergy)

        // One of the stats should increase
        val statIncreased = (updatedCharacter.str > initialStr) ||
                            (updatedCharacter.intVal > initialInt) ||
                            (updatedCharacter.dex > initialDex)
        assertEquals(true, statIncreased)

        // Exp should increase
        assertEquals(initialCharacter.exp + 10, updatedCharacter.exp)
    }

    @Test
    fun `rest restores energy`() = runTest {
         viewModel.createCharacter("TestHero", CharacterClass.WARRIOR)
         testDispatcher.scheduler.advanceUntilIdle()

         // Train to reduce energy
         viewModel.train()
         testDispatcher.scheduler.advanceUntilIdle()

         val drainedCharacter = viewModel.characterState.value!!
         assert(drainedCharacter.currentEnergy < drainedCharacter.maxEnergy)

         viewModel.rest()
         testDispatcher.scheduler.advanceUntilIdle()

         val restedCharacter = viewModel.characterState.value!!
         assertEquals(restedCharacter.maxEnergy, restedCharacter.currentEnergy)
    }
}
