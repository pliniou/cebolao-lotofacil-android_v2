package com.cebolao.lotofacil.domain.model

import com.cebolao.lotofacil.domain.GameConstants

/**
 * Interface para unificar o cálculo de estatísticas comuns
 * entre um jogo gerado (LotofacilGame) e um sorteio histórico (Draw).
 */
interface GameStatisticsProvider {
    val numbers: Set<Int>

    val sum: Int get() = if (this is BitmaskProvider) MaskUtils.sumFromMask(mask) else numbers.sum()
    val evens: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { it % 2 == 0 } else numbers.count { it % 2 == 0 }
    val odds: Int get() = GameConstants.GAME_SIZE - evens
    val primes: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { GameConstants.PRIMOS.contains(it) } else numbers.count { it in GameConstants.PRIMOS }
    val fibonacci: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { GameConstants.FIBONACCI.contains(it) } else numbers.count { it in GameConstants.FIBONACCI }
    val frame: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { GameConstants.MOLDURA.contains(it) } else numbers.count { it in GameConstants.MOLDURA }
    // Removed Portrait and MultiplesOf3

    /** Número de sequências consecutivas de 3+ números */
    val sequences: Int
        get() {
            val present = BooleanArray(GameConstants.MAX_NUMBER + 1)
            if (this is BitmaskProvider) {
                MaskUtils.forEachNumber(mask) { n -> if (n in GameConstants.NUMBER_RANGE) present[n] = true }
            } else {
                for (n in numbers) if (n in GameConstants.NUMBER_RANGE) present[n] = true
            }

            var run = 0
            var count = 0
            for (n in GameConstants.MIN_NUMBER..GameConstants.MAX_NUMBER) {
                if (present[n]) {
                    run++
                } else {
                    if (run >= 3) count++
                    run = 0
                }
            }
            if (run >= 3) count++
            return count
        }

    /** Número de linhas horizontais com 3+ números (grid 5x5) */
    val lines: Int
        get() {
            val rowCounts = IntArray(5)
            if (this is BitmaskProvider) {
                MaskUtils.forEachNumber(mask) { n ->
                    val row = (n - 1) / 5
                    if (row in 0..4) rowCounts[row]++
                }
            } else {
                for (n in numbers) {
                    val row = (n - 1) / 5
                    if (row in 0..4) rowCounts[row]++
                }
            }
            var count = 0
            for (c in rowCounts) if (c >= 3) count++
            return count
        }

    /** Número de colunas verticais com 3+ números (grid 5x5) */
    val columns: Int
        get() {
            val colCounts = IntArray(5)
            if (this is BitmaskProvider) {
                MaskUtils.forEachNumber(mask) { n ->
                    val col = (n - 1) % 5
                    if (col in 0..4) colCounts[col]++
                }
            } else {
                for (n in numbers) {
                    val col = (n - 1) % 5
                    if (col in 0..4) colCounts[col]++
                }
            }
            var count = 0
            for (c in colCounts) if (c >= 3) count++
            return count
        }

    /** Número de quadrantes (4 regiões) que possuem pelo menos 1 número selecionado (0..4)
     *  Grid 5x5 dividido em 4 quadrantes (top-left, top-right, bottom-left, bottom-right).
     */
    val quadrants: Int
        get() {
            val present = BooleanArray(4)
            if (this is BitmaskProvider) {
                MaskUtils.forEachNumber(mask) { n ->
                    val row = (n - 1) / 5
                    val col = (n - 1) % 5
                    val qr = if (row < 2) 0 else 1
                    val qc = if (col < 2) 0 else 1
                    val q = qr * 2 + qc
                    if (q in 0..3) present[q] = true
                }
            } else {
                for (n in numbers) {
                    val row = (n - 1) / 5
                    val col = (n - 1) % 5
                    val qr = if (row < 2) 0 else 1
                    val qc = if (col < 2) 0 else 1
                    val q = qr * 2 + qc
                    if (q in 0..3) present[q] = true
                }
            }
            var count = 0
            for (p in present) if (p) count++
            return count
        }

    /** Quantidade de números do jogo que estão contidos em um sorteio anterior */
    fun repeatedFrom(lastDraw: Set<Int>?): Int {
        val base = lastDraw ?: return 0
        // Otimização: usar bitmasks quando disponível
        if (this is BitmaskProvider) {
            val lastDrawMask = MaskUtils.toMask(base)
            return MaskUtils.intersectCount(mask, lastDrawMask)
        }
        return numbers.intersect(base).size
    }
}
