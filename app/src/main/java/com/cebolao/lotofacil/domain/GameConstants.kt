package com.cebolao.lotofacil.domain

import java.math.BigDecimal

object GameConstants {
    const val GAME_SIZE = 15
    const val MIN_NUMBER = 1
    const val MAX_NUMBER = 25
    const val MIN_PRIZE_SCORE = 11
    const val HISTORY_CHECK_SIZE = 10
    const val SUM_MIN_RANGE = 120
    const val SUM_MAX_RANGE = 270
    const val SUM_STEP = 10
    const val GRID_COLUMNS = 5

    val TIME_WINDOWS: List<Int> = listOf(0, 20, 50, 75, 100, 200, 500)
    val GAME_QUANTITY_OPTIONS: List<Int> = (1..10).toList()
    val NUMBER_RANGE: IntRange = MIN_NUMBER..MAX_NUMBER
    val ALL_NUMBERS: List<Int> = NUMBER_RANGE.toList()
    val GAME_COST: BigDecimal = BigDecimal("3.50")
    val PRIMOS: Set<Int> = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23)
    val FIBONACCI: Set<Int> = setOf(1, 2, 3, 5, 8, 13, 21)
    val MOLDURA: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 10, 11, 15, 16, 20, 21, 22, 23, 24, 25)
    val MIOLO: Set<Int> = setOf(7, 8, 9, 12, 13, 14, 17, 18, 19)
    val MULTIPLOS_DE_3: Set<Int> = setOf(3, 6, 9, 12, 15, 18, 21, 24)

    // Analysis Ranges
    val SUM_IDEAL = 180..210
    val SUM_ACCEPTABLE = 160..230
    val EVENS_IDEAL = 7..8
    val EVENS_ACCEPTABLE = 6..9
    val PRIMES_IDEAL = 4..6
    val PRIMES_ACCEPTABLE = 3..7
    val FRAME_IDEAL = 8..11
    val FRAME_ACCEPTABLE = 7..12

    // Financial Prizes (Estimated)
    val PRIZE_11 = BigDecimal("6.00")
    val PRIZE_12 = BigDecimal("12.00")
    val PRIZE_13 = BigDecimal("30.00")
    val PRIZE_14 = BigDecimal("1500.00")
    val PRIZE_15 = BigDecimal("1500000.00")
}
