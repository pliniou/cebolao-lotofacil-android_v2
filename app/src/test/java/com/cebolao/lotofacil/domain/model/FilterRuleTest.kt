package com.cebolao.lotofacil.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterRuleTest {

    @Test
    fun `CountRangeFilter matches correctly`() {
        val filter = CountRangeFilter(FilterType.PARES, 5..7) { it.evens }
        
        val metricsIn = GameComputedMetrics(
            sum = 0, evens = 6, primes = 0, frame = 0,
            fibonacci = 0, repeated = 0, sequences = 0,
            lines = 0, columns = 0, quadrants = 0
        )
        
        val metricsOutLow = metricsIn.copy(evens = 4)
        val metricsOutHigh = metricsIn.copy(evens = 8)

        assertTrue(filter.matches(metricsIn))
        assertFalse(filter.matches(metricsOutLow))
        assertFalse(filter.matches(metricsOutHigh))
    }

    @Test
    fun `toRule returns correct filter type`() {
        val state = FilterState(
            type = FilterType.PRIMOS,
            isEnabled = true,
            selectedRange = 4f..6f
        )
        
        val rule = state.toRule()
        
        assertTrue(rule is CountRangeFilter)
        assertTrue(rule?.type == FilterType.PRIMOS)
    }
    
    @Test
    fun `toRule returns null when disabled`() {
        val state = FilterState(
            type = FilterType.PRIMOS,
            isEnabled = false,
            selectedRange = 4f..6f
        )
        
        assertTrue(state.toRule() == null)
    }
}
