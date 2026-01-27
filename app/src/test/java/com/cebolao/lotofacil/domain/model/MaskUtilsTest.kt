package com.cebolao.lotofacil.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaskUtilsTest {

    @Test
    fun `toMask creates correct bitmask for single number`() {
        val mask = MaskUtils.toMask(setOf(1))
        assertEquals(1L, mask)
    }

    @Test
    fun `toMask creates correct bitmask for multiple numbers`() {
        val numbers = setOf(1, 2, 3, 4, 5)
        val mask = MaskUtils.toMask(numbers)
        // 1<<0 | 1<<1 | 1<<2 | 1<<3 | 1<<4 = 1 + 2 + 4 + 8 + 16 = 31
        assertEquals(31L, mask)
    }

    @Test
    fun `toMask handles full game (15 numbers)`() {
        val numbers = (1..15).toSet()
        val mask = MaskUtils.toMask(numbers)
        assertEquals(15, MaskUtils.popcount(mask))
    }

    @Test
    fun `toSet reconstructs numbers from mask`() {
        val original = setOf(1, 5, 10, 15, 20)
        val mask = MaskUtils.toMask(original)
        val reconstructed = MaskUtils.toSet(mask)
        assertEquals(original, reconstructed)
    }

    @Test
    fun `toSet handles empty mask`() {
        val result = MaskUtils.toSet(0L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `popcount returns correct count`() {
        val mask = MaskUtils.toMask(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        assertEquals(10, MaskUtils.popcount(mask))
    }

    @Test
    fun `popcount returns zero for empty mask`() {
        assertEquals(0, MaskUtils.popcount(0L))
    }

    @Test
    fun `sumFromMask calculates correct sum`() {
        val numbers = setOf(1, 2, 3)
        val mask = MaskUtils.toMask(numbers)
        assertEquals(6, MaskUtils.sumFromMask(mask))
    }

    @Test
    fun `intersectCount returns correct intersection count`() {
        val mask1 = MaskUtils.toMask(setOf(1, 2, 3, 4, 5))
        val mask2 = MaskUtils.toMask(setOf(3, 4, 5, 6, 7))
        // Intersection is {3, 4, 5} = 3 elements
        assertEquals(3, MaskUtils.intersectCount(mask1, mask2))
    }

    @Test
    fun `intersectCount returns zero for disjoint sets`() {
        val mask1 = MaskUtils.toMask(setOf(1, 2, 3))
        val mask2 = MaskUtils.toMask(setOf(10, 11, 12))
        assertEquals(0, MaskUtils.intersectCount(mask1, mask2))
    }

    @Test
    fun `intersectCount returns full count for identical masks`() {
        val mask = MaskUtils.toMask(setOf(1, 2, 3, 4, 5))
        assertEquals(5, MaskUtils.intersectCount(mask, mask))
    }

    @Test
    fun `forEachNumber iterates all set bits`() {
        val numbers = setOf(5, 10, 15)
        val mask = MaskUtils.toMask(numbers)
        val collected = mutableListOf<Int>()
        MaskUtils.forEachNumber(mask) { collected.add(it) }
        assertEquals(listOf(5, 10, 15), collected.sorted())
    }

    @Test
    fun `countIf counts only matching predicates`() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val mask = MaskUtils.toMask(numbers)
        // Count even numbers
        val evenCount = MaskUtils.countIf(mask) { it % 2 == 0 }
        assertEquals(5, evenCount)
    }

    @Test
    fun `toMask handles maximum number 25`() {
        val mask = MaskUtils.toMask(setOf(25))
        // 1 << 24 = 16777216
        assertEquals(1L shl 24, mask)
    }

    @Test
    fun `toMask handles all 25 numbers`() {
        val allNumbers = (1..25).toSet()
        val mask = MaskUtils.toMask(allNumbers)
        assertEquals(25, MaskUtils.popcount(mask))
        assertEquals(25, MaskUtils.toSet(mask).size)
    }
}
