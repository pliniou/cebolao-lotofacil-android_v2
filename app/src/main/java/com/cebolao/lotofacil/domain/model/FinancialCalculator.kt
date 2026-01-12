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
     * Calcula projeção financeira com custo real do jogo e dados históricos opcionais
     * 
     * @param checkResult Resultado da conferência
     * @param totalDraws Total de concursos analisados
     * @param gameCost Custo real do jogo (não usar constante fixa)
     * @param historicalData Dados históricos opcionais para cálculos mais precisos
     * @return FinancialProjection com flag isApproximate se não houver dados históricos
     */
    fun calculate(
        checkResult: CheckResult,
        totalDraws: Int,
        gameCost: BigDecimal,
        historicalData: List<Draw>? = null
    ): FinancialProjection {
        val cost = gameCost.multiply(BigDecimal(totalDraws))

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

        val profit = revenue.subtract(cost)
        val roi = if (cost > BigDecimal.ZERO) {
            profit.divide(cost, 4, RoundingMode.HALF_UP).toFloat() * 100
        } else 0f

        val isApproximate = historicalData.isNullOrEmpty()
        val disclaimer = if (isApproximate) {
            "Projeção aproximada baseada em valores médios estimados. Valores reais podem variar."
        } else null

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
     * Método de compatibilidade mantido para código legado
     * @deprecated Use calculate com gameCost e historicalData
     */
    @Deprecated("Use calculate with gameCost and historicalData parameters")
    fun calculate(checkResult: CheckResult, totalDraws: Int): FinancialProjection {
        return calculate(
            checkResult = checkResult,
            totalDraws = totalDraws,
            gameCost = GameConstants.GAME_COST,
            historicalData = null
        )
    }
}
