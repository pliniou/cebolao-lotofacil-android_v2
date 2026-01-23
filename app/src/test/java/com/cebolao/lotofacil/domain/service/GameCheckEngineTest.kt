package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.LotofacilGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GameCheckEngineTest {

    private lateinit var engine: GameCheckEngine

    @Before
    fun setup() {
        engine = GameCheckEngine()
    }

    @Test
    fun `checkGame returns empty result when history is empty`() {
        val game = LotofacilGame.fromNumbers(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))
        val result = engine.checkGame(game, emptyList())
        
        assertEquals(0, result.scoreCounts.size)
        assertEquals(0, result.lastCheckedContest)
    }

    @Test
    fun `checkGame correctly identifies hits`() {
        // Game with numbers 1 to 15
        val game = LotofacilGame.fromNumbers((1..15).toSet())
        
        // Draw with numbers 1 to 11 (11 hits)
        val draw11 = Draw.fromNumbers(1, (1..11).toSet() + (16..19).toSet(), 0L)
        
        // Draw with numbers 1 to 15 (15 hits)
        val draw15 = Draw.fromNumbers(2, (1..15).toSet(), 0L)
        
        val history = listOf(draw15, draw11)
        val result = engine.checkGame(game, history)
        
        assertEquals(1, result.scoreCounts[11])
        assertEquals(1, result.scoreCounts[15])
        assertEquals(2, result.lastHitContest)
        assertEquals(15, result.lastHitScore)
    }
}
