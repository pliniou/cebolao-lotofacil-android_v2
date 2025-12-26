package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.Draw
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsAnalyzerTest {

    private val analyzer = StatisticsAnalyzer(kotlinx.coroutines.test.UnconfinedTestDispatcher())

    @Test
    fun `analyze calculates correct frequencies and delays`() = kotlinx.coroutines.test.runTest {
        val history = listOf(
            Draw.fromNumbers(100, setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), java.util.Date().time), // Last draw
            Draw.fromNumbers(99, setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 25), java.util.Date().time), // 15 missing, 25 present
            Draw.fromNumbers(98, setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 25), java.util.Date().time)
        )

        // Run analysis on subset of 25 numbers
        val stats = analyzer.analyze(history)
        
        // Check Frequency
        val freq15 = stats.mostFrequentNumbers.find { it.number == 15 }
        val freq25 = stats.mostFrequentNumbers.find { it.number == 25 }
        
        assertEquals(1, freq15?.frequency)
        assertEquals(2, freq25?.frequency)
        
        // Check Delay (stored in frequency property of NumberFrequency in mostOverdueNumbers)
        val delay15 = stats.mostOverdueNumbers.find { it.number == 15 }
        val delay25 = stats.mostOverdueNumbers.find { it.number == 25 }
        
        assertEquals(0, delay15?.frequency)
        assertEquals(1, delay25?.frequency)
    }
}
