package com.cebolao.lotofacil.domain.model

import com.cebolao.lotofacil.domain.GameConstants
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class FinancialCalculatorTest {

    @Test
    fun `calculate earnings returns correct prize for 11 hits`() {
        val cost = BigDecimal("3.00")
        // 1 game checked, 1 result with 11 hits
        val checkResult = CheckResult(
            scoreCounts = persistentMapOf(11 to 1),
            lastCheckedContest = 100
        )
        
        val result = FinancialCalculator.calculate(checkResult, 1, cost)
        
        // Revenue = 6.00
        // Cost = 3.00
        // Profit = 3.00
        val expectedProfit = GameConstants.PRIZE_11.subtract(cost)
        
        assertEquals(expectedProfit, result.profitDecimal)
        assertEquals(GameConstants.PRIZE_11, result.revenueDecimal)
    }

    @Test
    fun `calculate earnings returns correct prize for 15 hits`() {
        val cost = BigDecimal("3.00")
        val checkResult = CheckResult(
            scoreCounts = persistentMapOf(15 to 1),
            lastCheckedContest = 100
        )
        
        val result = FinancialCalculator.calculate(checkResult, 1, cost)
        
        assertEquals(GameConstants.PRIZE_15, result.revenueDecimal)
    }

    @Test
    fun `calculate earnings returns negative for loss`() {
        val cost = BigDecimal("3.00")
        val checkResult = CheckResult(
            scoreCounts = persistentMapOf(10 to 1), // 10 hits = 0 prize
            lastCheckedContest = 100
        )
        
        val result = FinancialCalculator.calculate(checkResult, 1, cost)
        
        assertEquals(BigDecimal.ZERO, result.revenueDecimal)
        assertEquals(cost.negate(), result.profitDecimal)
    }
}
