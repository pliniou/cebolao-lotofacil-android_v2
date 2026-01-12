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
    val primes: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { it in GameConstants.PRIMOS } else numbers.count { it in GameConstants.PRIMOS }
    val fibonacci: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { it in GameConstants.FIBONACCI } else numbers.count { it in GameConstants.FIBONACCI }
    val frame: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { it in GameConstants.MOLDURA } else numbers.count { it in GameConstants.MOLDURA }
    val multiplesOf3: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { it % 3 == 0 } else numbers.count { it % 3 == 0 }
    val center: Int get() = if (this is BitmaskProvider) MaskUtils.countIf(mask) { it in GameConstants.MIOLO } else numbers.count { it in GameConstants.MIOLO }

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
    
    /** Quantidade de números do jogo que estão contidos em um sorteio anterior */
    fun repeatedFrom(lastDraw: Set<Int>?): Int {
        val base = lastDraw ?: return 0
        if (this is BitmaskProvider) {
            val lastDrawMask = MaskUtils.toMask(base)
            return MaskUtils.intersectCount(mask, lastDrawMask)
        }
        return numbers.intersect(base).size
    }
}
