package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import com.cebolao.lotofacil.domain.GameConstants
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FinancialProjection(
    val investment: String,
    val revenue: String,
    val profit: String,
    val roi: Float,
    val breakEven: Boolean,
    val isApproximate: Boolean = false,
    val disclaimer: String? = null
) {

    companion object {
        fun fromBigDecimal(
            investment: BigDecimal,
            revenue: BigDecimal,
            profit: BigDecimal,
            roi: Float,
            breakEven: Boolean,
            isApproximate: Boolean = false,
            disclaimer: String? = null
        ): FinancialProjection {
            return FinancialProjection(
                investment = investment.toString(),
                revenue = revenue.toString(),
                profit = profit.toString(),
                roi = roi,
                breakEven = breakEven,
                isApproximate = isApproximate,
                disclaimer = disclaimer
            )
        }
    }
}

object FinancialCalculator {

    /**
     * Calculates a financial projection for a given set of lottery check results.
     *
     * This method no longer relies on a fixed game cost nor assumes the absence of
     * historical data.  Callers must provide the real cost per ticket, and may
     * supply the full history of draws to produce an exact projection.  When the
     * historical draw list is null or empty the projection is flagged as
     * approximate and a disclaimer is provided.
     *
     * @param checkResult    aggregated scoring results from evaluating a ticket
     * @param totalDraws     number of draws considered while generating the report
     * @param gameCost       actual cost of a single game; do not pass a constant
     * @param historicalData optional list of historic draws; when null or empty the
     *                       returned [FinancialProjection] is marked approximate
     * @return a [FinancialProjection] containing investment, revenue, profit, ROI,
     *         and approximation metadata
     */
    fun calculate(
        checkResult: CheckResult,
        totalDraws: Int,
        gameCost: BigDecimal,
        historicalData: List<Draw>? = null
    ): FinancialProjection {
        // Compute total investment by multiplying the cost per game by the number of draws
        val cost = gameCost.multiply(BigDecimal(totalDraws))

        // Aggregate revenue by summing prize amounts per hit score
        val revenue = checkResult.scoreCounts.entries.sumOf { (score, count) ->
            val prize = when (score) {
                11 -> GameConstants.FinancialPrizes.PRIZE_11
                12 -> GameConstants.FinancialPrizes.PRIZE_12
                13 -> GameConstants.FinancialPrizes.PRIZE_13
                14 -> GameConstants.FinancialPrizes.PRIZE_14
                15 -> GameConstants.FinancialPrizes.PRIZE_15
                else -> BigDecimal.ZERO
            }
            prize.multiply(BigDecimal(count))
        }

        // Derive profit as the difference between revenue and cost
        val profit = revenue.subtract(cost)

        // Avoid division by zero when computing return on investment (ROI)
        val roi = if (cost > BigDecimal.ZERO) {
            profit.divide(cost, 4, RoundingMode.HALF_UP).toFloat() * 100
        } else {
            0f
        }

        // Flag the projection as approximate when historical data was not provided
        val isApproximate = historicalData.isNullOrEmpty()
        val disclaimer = if (isApproximate) {
            "Projeção aproximada baseada em valores médios estimados. Valores reais podem variar."
        } else {
            null
        }

        return FinancialProjection.fromBigDecimal(
            investment = cost,
            revenue = revenue,
            profit = profit,
            roi = roi,
            breakEven = profit >= BigDecimal.ZERO,
            isApproximate = isApproximate,
            disclaimer = disclaimer
        )
    }

    /**
     * Calculates the number of combinations (n choose k).
     */
    fun combinations(n: Int, k: Int): Long {
        if (k < 0 || k > n) return 0
        if (k == 0 || k == n) return 1
        var newK = k
        if (newK > n / 2) newK = n - newK
        var result: Long = 1
        for (i in 1..newK) {
            result = result * (n - i + 1) / i
        }
        return result
    }

    /**
     * Calculates the cost of a bet based on the number of selected numbers.
     * Lotofácil rules: minimum 15, maximum 20.
     * The cost is equivalent to the number of 15-number combinations possible.
     */
    fun getGameCost(numbersSelected: Int): BigDecimal {
        if (numbersSelected < 15) return BigDecimal.ZERO
        val numCombinations = combinations(numbersSelected, 15)
        return GameConstants.GAME_COST.multiply(BigDecimal(numCombinations))
    }
}
