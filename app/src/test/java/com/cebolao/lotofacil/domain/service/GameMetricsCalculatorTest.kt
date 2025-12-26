package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.LotofacilGame
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMetricsCalculatorTest {

    private val calculator = GameMetricsCalculator()

    @Test
    fun `calculate metrics returns correct values`() {
        // Game: 1, 2, 3, 4, 5, 11, 12, 13, 14, 15, 21, 22, 23, 24, 25
        val numbers = setOf(1, 2, 3, 4, 5, 11, 12, 13, 14, 15, 21, 22, 23, 24, 25)
        val game = LotofacilGame.fromNumbers(numbers)
        val lastDraw = setOf(1, 2, 3, 6, 7, 11, 12, 18, 19, 21, 22, 23, 24, 25, 10) // 11 hits

        val metrics = calculator.calculate(game, lastDraw)

        assertEquals(195, metrics.sum) // Sum: 15*3 + (1+2+3+4+5) + (1+2+3+4+5) + (1+2+3+4+5) + 30 + 60 = 15+15+15 + 30 + 60 + 10x... wait. 
        // 1+2+3+4+5 = 15
        // 11+12+13+14+15 = 65
        // 21+22+23+24+25 = 115
        // Total = 195. Correct.
        
        // Evens: 2, 4, 12, 14, 22, 24 -> 6 evens
        assertEquals(6, metrics.evens)

        // Primes: 2, 3, 5, 11, 13, 23 -> 6 primes
        assertEquals(6, metrics.primes)
        
        // Frame (Moldura): 1, 2, 3, 4, 5, 11, 15, 21, 22, 23, 24, 25 -> 12
        assertEquals(12, metrics.frame)
        
        // Fibonacci: 1, 2, 3, 5, 13, 21 -> 6
        assertEquals(6, metrics.fibonacci)
        
        // Repeated: 1, 2, 3, 11, 12, 21, 22, 23, 24, 25 -> 10
        assertEquals(10, metrics.repeated)

        // Multiples of 3: 3, 12, 15, 21, 24 -> 5
        assertEquals(5, metrics.multiplesOf3)

        // Center: 12, 13, 14 -> 3
        assertEquals(3, metrics.center)
    }
}
