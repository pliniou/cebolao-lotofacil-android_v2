package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameGeneratorTest {

    private val historyRepository: HistoryRepository = mockk()
    // No history for these tests
    private val emptyHistory = emptyList<Draw>()

    init {
        io.mockk.coEvery { historyRepository.getHistory() } returns emptyHistory
    }

    @Test
    fun `generate produces exact quantity of games`() = runTest {
        val generator = GameGenerator(historyRepository)
        val result = generator.generate(quantity = 10, filters = emptyList()).toList()
        
        val finalProgress = result.last()
        val finishedState = finalProgress.progressType as GenerationProgressType.Finished
        assertEquals(10, finishedState.games.size)
    }

    @Test
    fun `property test generated games respect active filters`() = runTest {
        // Rule: Evens must be between 6 and 8
        val evenFilter = FilterState(
            type = FilterType.PARES,
            isEnabled = true,
            selectedRange = 6f..8f
        )
        
        val generator = GameGenerator(historyRepository)
        // Fixed seed for determinism
        val result = generator.generate(quantity = 50, filters = listOf(evenFilter), seed = 12345L).toList()
        
        val finalProgress = result.last()
        val finishedState = finalProgress.progressType as GenerationProgressType.Finished
        val games = finishedState.games
        
        val calculator = GameMetricsCalculator()
        
        assertTrue(games.isNotEmpty())
        
        games.forEach { game ->
            val metrics = calculator.calculate(game, null)
            val evens = metrics.evens
            assertTrue("Game ${game.numbers} has $evens evens, expected 6..8", evens in 6..8)
        }
    }
    
    // Helper to mock flow return
    // flowOf is already imported
}
