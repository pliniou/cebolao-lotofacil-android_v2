package com.cebolao.lotofacil.domain.service.solver

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BacktrackingSolverTest {

    @Test
    fun `findValidGame returns game when no filters are active`() = runBlocking {
        val filters = emptyList<FilterState>()
        val solver = BacktrackingSolver(
            filters = filters,
            lastDrawNumbers = emptySet(),
            rnd = Random(123),
            onAttempt = {},
            onRejection = {}
        )

        val game = solver.findValidGame()

        assertNotNull("Should find a game with no filters", game)
        assertEquals(15, game!!.numbers.size)
        assertTrue(game.numbers.sorted() == game.numbers.toList())
    }

    @Test
    fun `findValidGame respects sum range filter`() = runBlocking {
        // Range tight: 180 to 190
        val targetRange = 180f..190f
        val filters = listOf(
            FilterState(
                type = FilterType.SOMA_DEZENAS,
                isEnabled = true,
                selectedRange = targetRange
            )
        )
        val solver = BacktrackingSolver(
            filters = filters,
            lastDrawNumbers = emptySet(),
            rnd = Random(456),
            onAttempt = {},
            onRejection = {}
        )

        val game = solver.findValidGame()

        assertNotNull("Should find a game within sum range", game)
        val sum = game!!.numbers.sum()
        assertTrue("Sum $sum should be in $targetRange", sum >= targetRange.start && sum <= targetRange.endInclusive)
    }

    @Test
    fun `findValidGame returns null for impossible constraints`() = runBlocking {
        // Impossible: Sum of 15 unique numbers (1-25) cannot be < 120 (min sum is 1+..+15 = 120)
        // Let's force something effectively impossible with conflicting filters or extreme bounds
        // Min sum is 120. Max sum is 25+..+11 = 270.
        // Let's ask for sum < 100 which is mathematically impossible
        val filters = listOf(
            FilterState(
                type = FilterType.SOMA_DEZENAS,
                isEnabled = true,
                selectedRange = 10f..100f
            )
        )

        val solver = BacktrackingSolver(
            filters = filters,
            lastDrawNumbers = emptySet(),
            rnd = Random(789),
            onAttempt = {},
            onRejection = {}
        )

        val game = solver.findValidGame()
        assertEquals("Should fail to find game for impossible constraints", null, game)
    }
    
    @Test
    fun `findValidGame respects even count filter`() = runBlocking {
        // Pares = 7 or 8 (common filter)
        val targetRange = 7f..8f
        val filters = listOf(
            FilterState(
                type = FilterType.PARES,
                isEnabled = true,
                selectedRange = targetRange
            )
        )
        
        val solver = BacktrackingSolver(
            filters = filters,
            lastDrawNumbers = emptySet(),
            rnd = Random(111),
            onAttempt = {},
            onRejection = {}
        )
        
        val game = solver.findValidGame()
        assertNotNull(game)
        
        val evens = game!!.numbers.count { it % 2 == 0 }
        assertTrue(
            "Evens count $evens should be in $targetRange",
            evens >= targetRange.start && evens <= targetRange.endInclusive
        )
    }
}
