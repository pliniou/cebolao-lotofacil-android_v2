package com.cebolao.lotofacil.domain.model

/** Utilities para representar jogos como bitmask (bits 0..24 correspondem aos números 1..25) */
object MaskUtils {
    fun toMask(numbers: Set<Int>): Long {
        var m = 0L
        for (n in numbers) {
            val idx = n - 1
            if (idx in 0..63) m = m or (1L shl idx)
        }
        return m
    }

    fun toSet(mask: Long): Set<Int> {
        val result = LinkedHashSet<Int>()
        var m = mask
        var idx = 1
        while (m != 0L && idx <= 64) {
            if ((m and 1L) != 0L) result.add(idx)
            m = m shr 1
            idx++
        }
        return result
    }

    fun popcount(mask: Long): Int {
        return java.lang.Long.bitCount(mask)
    }

    fun sumFromMask(mask: Long): Int {
        var m = mask
        var idx = 1
        var sum = 0
        while (m != 0L) {
            if ((m and 1L) != 0L) sum += idx
            m = m shr 1
            idx++
        }
        return sum
    }

    inline fun forEachNumber(mask: Long, action: (Int) -> Unit) {
        var m = mask
        var idx = 1
        while (m != 0L) {
            if ((m and 1L) != 0L) action(idx)
            m = m shr 1
            idx++
        }
    }

    inline fun countIf(mask: Long, predicate: (Int) -> Boolean): Int {
        var m = mask
        var idx = 1
        var c = 0
        while (m != 0L) {
            if ((m and 1L) != 0L && predicate(idx)) c++
            m = m shr 1
            idx++
        }
        return c
    }

    /**
     * Conta quantos números estão presentes em ambas as máscaras (interseção).
     * Operação O(1) usando operações bitwise.
     */
    fun intersectCount(mask1: Long, mask2: Long): Int {
        return popcount(mask1 and mask2)
    }
}
