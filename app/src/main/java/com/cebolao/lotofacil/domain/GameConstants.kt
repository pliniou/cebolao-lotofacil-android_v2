package com.cebolao.lotofacil.domain

import java.math.BigDecimal

object GameConstants {
    // General Game Settings
    const val GAME_SIZE = 15
    const val MIN_NUMBER = 1
    const val MAX_NUMBER = 25
    const val MIN_PRIZE_SCORE = 11
    const val HISTORY_CHECK_SIZE = 10
    const val GRID_COLUMNS = 5

    // Number Ranges
    val NUMBER_RANGE: IntRange = MIN_NUMBER..MAX_NUMBER
    val ALL_NUMBERS: List<Int> = NUMBER_RANGE.toList()

    // Game Cost
    val GAME_COST: BigDecimal = BigDecimal("3.50")

    // Mathematical Sets
    val PRIMOS: Set<Int> = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23)
    val FIBONACCI: Set<Int> = setOf(1, 2, 3, 5, 8, 13, 21)
    val MOLDURA: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 10, 11, 15, 16, 20, 21, 22, 23, 24, 25)
    val MIOLO: Set<Int> = setOf(7, 8, 9, 12, 13, 14, 17, 18, 19)
    val MULTIPLOS_DE_3: Set<Int> = setOf(3, 6, 9, 12, 15, 18, 21, 24)

    // Analysis Ranges
    object AnalysisRanges {
        val SUM_IDEAL = 180..210
        val SUM_ACCEPTABLE = 160..230
        val EVENS_IDEAL = 7..8
        val EVENS_ACCEPTABLE = 6..9
        val PRIMES_IDEAL = 4..6
        val PRIMES_ACCEPTABLE = 3..7
        val FRAME_IDEAL = 8..11
        val FRAME_ACCEPTABLE = 7..12
        val FIBONACCI_IDEAL = 3..5
        val FIBONACCI_ACCEPTABLE = 2..6
        val MULTIPLES_OF_3_IDEAL = 4..6
        val MULTIPLES_OF_3_ACCEPTABLE = 3..7
        val CENTER_IDEAL = 3..5
        val CENTER_ACCEPTABLE = 2..6
        val REPEATED_IDEAL = 8..10
        val REPEATED_ACCEPTABLE = 7..11
        val SEQUENCES_IDEAL = 3..5
        val SEQUENCES_ACCEPTABLE = 2..6
    }

    // Financial Prizes (Estimated)
    object FinancialPrizes {
        val PRIZE_11 = BigDecimal("6.00")
        val PRIZE_12 = BigDecimal("12.00")
        val PRIZE_13 = BigDecimal("30.00")
        val PRIZE_14 = BigDecimal("1500.00")
        val PRIZE_15 = BigDecimal("1500000.00")
    }

    // Game Generation Options
    const val MAX_GENERATION_QUANTITY = 50
    const val DEFAULT_GENERATION_QUANTITY = 5
    // Statistics Time Windows (number of contests to analyze)
    val TIME_WINDOWS = listOf(10, 20, 50, 100, 200, 500)

    // Sum Distribution for Charts
    const val SUM_STEP = 10
    const val SUM_MIN_RANGE = 150
    const val SUM_MAX_RANGE = 250
}
