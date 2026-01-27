package com.cebolao.lotofacil.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrawTest {

    @Test
    fun `fromNumbers creates draw with correct values`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val draw = Draw.fromNumbers(contestNumber = 3000, numbers = numbers, date = 1700000000000L)

        assertEquals(3000, draw.contestNumber)
        assertEquals(numbers, draw.numbers)
        assertEquals(1700000000000L, draw.date)
    }

    @Test
    fun `fromNumbers allows null date`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val draw = Draw.fromNumbers(contestNumber = 3000, numbers = numbers)

        assertNull(draw.date)
    }

    @Test
    fun `numbers property returns sorted set`() {
        val numbers = setOf(15, 10, 5, 20, 1)
        val draw = Draw.fromNumbers(contestNumber = 3000, numbers = numbers)

        val sortedNumbers = draw.numbers.toList()
        assertEquals(listOf(1, 5, 10, 15, 20), sortedNumbers)
    }

    @Test
    fun `mask property returns correct bitmask`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val draw = Draw.fromNumbers(contestNumber = 3000, numbers = numbers)

        val expectedMask = MaskUtils.toMask(numbers)
        assertEquals(expectedMask, draw.mask)
    }

    @Test
    fun `draw with same numbers has same mask`() {
        val numbers1 = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val numbers2 = setOf(15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)

        val draw1 = Draw.fromNumbers(contestNumber = 3000, numbers = numbers1)
        val draw2 = Draw.fromNumbers(contestNumber = 3001, numbers = numbers2)

        assertEquals(draw1.mask, draw2.mask)
    }

    @Test
    fun `different contest numbers are independent`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val draw1 = Draw.fromNumbers(contestNumber = 3000, numbers = numbers)
        val draw2 = Draw.fromNumbers(contestNumber = 3001, numbers = numbers)

        assertEquals(3000, draw1.contestNumber)
        assertEquals(3001, draw2.contestNumber)
    }
}
