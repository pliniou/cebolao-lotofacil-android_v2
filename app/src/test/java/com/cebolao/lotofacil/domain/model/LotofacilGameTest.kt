package com.cebolao.lotofacil.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LotofacilGameTest {

    @Test
    fun `fromNumbers creates game with correct mask`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val game = LotofacilGame.fromNumbers(numbers)

        assertEquals(15, game.numbers.size)
        assertEquals(numbers, game.numbers)
        assertFalse(game.isPinned)
    }

    @Test
    fun `fromNumbers preserves pinned state`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val game = LotofacilGame.fromNumbers(numbers, isPinned = true)

        assertTrue(game.isPinned)
    }

    @Test
    fun `fromMask reconstructs game correctly`() {
        val original = LotofacilGame.fromNumbers(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))
        val reconstructed = LotofacilGame.fromMask(original.mask)

        assertEquals(original.numbers, reconstructed.numbers)
        assertEquals(original.mask, reconstructed.mask)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromNumbers throws for less than 15 numbers`() {
        LotofacilGame.fromNumbers(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromNumbers throws for more than 15 numbers`() {
        LotofacilGame.fromNumbers(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromNumbers throws for number out of range`() {
        LotofacilGame.fromNumbers(setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromNumbers throws for number greater than 25`() {
        LotofacilGame.fromNumbers(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 26))
    }

    @Test
    fun `repeatedFrom counts correct matches with last draw`() {
        val game = LotofacilGame.fromNumbers(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))
        val lastDraw = setOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24)

        val repeated = game.repeatedFrom(lastDraw)

        // Intersection is {10, 11, 12, 13, 14, 15} = 6 numbers
        assertEquals(6, repeated)
    }

    @Test
    fun `repeatedFrom returns zero for null last draw`() {
        val game = LotofacilGame.fromNumbers(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))

        val repeated = game.repeatedFrom(null)

        assertEquals(0, repeated)
    }

    @Test
    fun `mask property returns correct value`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val game = LotofacilGame.fromNumbers(numbers)

        val expectedMask = MaskUtils.toMask(numbers)
        assertEquals(expectedMask, game.mask)
    }
}
