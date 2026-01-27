package com.cebolao.lotofacil.domain.model


/**
 * Complete hit range analysis statistics.
 * Calculated using the entire available database, not just the last 20 draws.
 */
data class RangeStatistics(
    val totalDraws: Int,
    val range0to5: Int,      // 0-5 hits
    val range6to10: Int,     // 6-10 hits
    val range11to15: Int,    // 11-15 hits
    val range16to20: Int,    // 16-20 hits
    val averageHits: Float,   // Average hits per game
    val standardDeviation: Float, // Standard deviation of hits
    val mostFrequentRange: String, // Most frequent range
    val leastFrequentRange: String // Least frequent range
) {
    /**
     * Calculates the percentage of games in a specific range
     */
    fun getRangePercentage(rangeCount: Int): Float {
        return if (totalDraws > 0) (rangeCount.toFloat() / totalDraws * 100) else 0f
    }

    /**
     * Returns the range with highest occurrence
     */
    fun getDominantRange(): String {
        val ranges = mapOf(
            "0-5" to range0to5,
            "6-10" to range6to10,
            "11-15" to range11to15,
            "16-20" to range16to20
        )
        return ranges.maxByOrNull { it.value }?.key ?: "N/A"
    }

    /**
     * Checks if a hit count is above average
     */
    fun isAboveAverage(hits: Int): Boolean {
        return hits > averageHits
    }

    /**
     * Classifies performance based on hit range
     */
    fun classifyPerformance(hits: Int): PerformanceLevel {
        return when {
            hits >= 16 -> PerformanceLevel.EXCELLENT
            hits >= 11 -> PerformanceLevel.GOOD
            hits >= 6 -> PerformanceLevel.AVERAGE
            hits >= 1 -> PerformanceLevel.BELOW_AVERAGE
            else -> PerformanceLevel.POOR
        }
    }
}

/**
 * Performance levels based on hit range
 */
enum class PerformanceLevel {
    POOR,
    BELOW_AVERAGE,
    AVERAGE,
    GOOD,
    EXCELLENT
}

/**
 * Utility for calculating range statistics from historical data
 */
object RangeStatisticsCalculator {

    /**
     * Calculates complete range statistics from a list of games
     */
    fun calculateFromGames(games: List<LotofacilGame>): RangeStatistics {
        if (games.isEmpty()) {
            return RangeStatistics(0, 0, 0, 0, 0, 0f, 0f, "N/A", "N/A")
        }

        val totalDraws = games.size

        // Count hits by range
        var range0to5 = 0
        var range6to10 = 0
        var range11to15 = 0
        var range16to20 = 0

        val hitsList = mutableListOf<Int>()

        games.forEach { game ->
            val hits = game.numbers.size
            hitsList.add(hits)

            when {
                hits <= 5 -> range0to5++
                hits <= 10 -> range6to10++
                hits <= 15 -> range11to15++
                else -> range16to20++
            }
        }

        // Calculate statistics
        val averageHits = hitsList.average().toFloat()
        val variance = hitsList.map { (it - averageHits).let { diff -> diff * diff } }.average()
        val standardDeviation = kotlin.math.sqrt(variance).toFloat()

        val ranges = mapOf(
            "0-5" to range0to5,
            "6-10" to range6to10,
            "11-15" to range11to15,
            "16-20" to range16to20
        )

        val mostFrequentRange = ranges.maxByOrNull { it.value }?.key ?: "N/A"
        val leastFrequentRange = ranges.minByOrNull { it.value }?.key ?: "N/A"

        return RangeStatistics(
            totalDraws = totalDraws,
            range0to5 = range0to5,
            range6to10 = range6to10,
            range11to15 = range11to15,
            range16to20 = range16to20,
            averageHits = averageHits,
            standardDeviation = standardDeviation,
            mostFrequentRange = mostFrequentRange,
            leastFrequentRange = leastFrequentRange
        )
    }

    /**
     * Calculates statistics from check report data
     */
    fun calculateFromCheckReports(reports: List<CheckReport>): RangeStatistics {
        val games = reports.map { it.ticket }
        return calculateFromGames(games)
    }
}
