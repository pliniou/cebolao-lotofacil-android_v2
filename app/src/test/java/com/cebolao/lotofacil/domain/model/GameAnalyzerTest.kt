package com.cebolao.lotofacil.domain.model

import com.cebolao.lotofacil.domain.GameConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameAnalyzerTest {

    @Test
    fun `analyze returns score range valid`() {
        val numbers = setOf(1,2,3,4,5,11,12,13,14,15,21,22,23,24,25) 
        val score = GameAnalyzer.analyze(numbers)
        
        // Verify it returns a score between 0 and 100
        assertTrue(score.totalScore in 0..100)
    }

    @Test
    fun `analyze ideal game returns high score`() {
         // Construct a game known to be reasonably good
         // 01 02 03 04 05 (Frame 5)
         // 11 12 13 14 15 (Frame 2, Portrait 3)
         // 21 22 23 24 25 (Frame 5) 
         // Frame = 12. 
         // Sum = 195 (Excellent)
         
         val numbers = setOf(1,2,3,4,5,11,12,13,14,15,21,22,23,24,25)
         val score = GameAnalyzer.analyze(numbers)
         
         // Should be reasonably high, at least > 50
         assertTrue(score.totalScore > 50)
    }
    
    @Test
    fun `analyze bad game returns low score`() {
        // Construct a game with poor metrics across categories
        // 13 Odds (all available) + 2 Evens (2, 4) -> Evens = 2 (Warning)
        // Primes: 2, 3, 5, 7, 11, 13, 17, 19, 23 (9 Primes) -> (Warning)
        // Frame: High (Most odds are on frame) -> (Warning)
        // Sum: ~175 (Acceptable/Good)
        // Adjusted to ensure Sum is also Warning (Low)
        // Set: 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 15, 17, 19, 21, 23
        // Sum: 156 (< 160 -> Warning).
        // Evens: 2, 4, 6 (3 -> Warning).
        // Primes: 9 (High -> Warning).
        // Frame: 10 (Excellent).
        // Total Points: 3+3+3+10 = 19. 19/40 = 47.5 < 50.
        val badNumbers = setOf(1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 15, 17, 19, 21, 23)
        val score = GameAnalyzer.analyze(badNumbers)
        
        // Should be bad (< 50)
        assertTrue("Score ${score.totalScore} should be < 50", score.totalScore < 50)
    }
}
