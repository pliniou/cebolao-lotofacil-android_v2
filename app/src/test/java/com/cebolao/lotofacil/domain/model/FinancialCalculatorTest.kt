package com.cebolao.lotofacil.domain.model

import com.cebolao.lotofacil.domain.GameConstants
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class FinancialCalculatorTest {

    @Test
    fun `calculate returns correct investment and revenue`() {
        val scoreCounts = mapOf(11 to 2, 12 to 1) // 11 points x 2, 12 points x 1
        val checkResult = CheckResult(
            scoreCounts = scoreCounts.toImmutableMap(),
            lastHitContest = null,
            lastHitScore = null,
            lastCheckedContest = 1,
            recentHits = persistentListOf()
        )
        
        val totalDraws = 10
        val gameCost = BigDecimal("3.00")
        
        val projection = FinancialCalculator.calculate(
            checkResult = checkResult,
            totalDraws = totalDraws,
            gameCost = gameCost
        )
        
        // Investment: 10 draws * 3.00 = 30.00
        assertEquals("30.00", projection.investment)
        
        // Revenue: (2 * PRIZE_11) + (1 * PRIZE_12)
        val expectedRevenue = GameConstants.FinancialPrizes.PRIZE_11.multiply(BigDecimal(2))
            .add(GameConstants.FinancialPrizes.PRIZE_12.multiply(BigDecimal(1)))
        
        assertEquals(expectedRevenue.toString(), projection.revenue)
    }

    @Test
    fun `combinations returns correct values`() {
        assertEquals(1, FinancialCalculator.combinations(15, 15))
        assertEquals(16, FinancialCalculator.combinations(16, 15))
        // 20 choose 15 = 15504
        assertEquals(15504, FinancialCalculator.combinations(20, 15))
    }

    @Test
    fun `getGameCost returns correct cost for 15 numbers`() {
        val cost = FinancialCalculator.getGameCost(15)
        assertEquals(GameConstants.GAME_COST, cost)
    }
}
