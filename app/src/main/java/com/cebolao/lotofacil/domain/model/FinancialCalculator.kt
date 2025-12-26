package com.cebolao.lotofacil.domain.model

import com.cebolao.lotofacil.domain.GameConstants
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class FinancialProjection(
    val investment: String, // BigDecimal serializado como String
    val revenue: String,
    val profit: String,
    val roi: Float, // Return on Investment %
    val breakEven: Boolean,
    val isApproximate: Boolean = false,
    val disclaimer: String? = null
) {
    // Propriedades auxiliares para compatibilidade
    val investmentDecimal: BigDecimal get() = BigDecimal(investment)
    val revenueDecimal: BigDecimal get() = BigDecimal(revenue)
    val profitDecimal: BigDecimal get() = BigDecimal(profit)
    
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
    
    // Estimated/Fixed prizes for simulation
    // Note: 14 and 15 vary a lot, using conservative averages
    // Estimated/Fixed prizes for simulation
    // Note: 14 and 15 vary a lot, using conservative averages
    // Values now in GameConstants 

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
        
        // Se houver dados históricos, podemos calcular prêmios médios reais
        // Por enquanto, Draw não contém informações de prêmios diretamente
        // Então usamos valores fixos e marcamos como aproximado se não houver dados
        val hasHistoricalData = historicalData != null && historicalData.isNotEmpty()
        
        var revenue = BigDecimal.ZERO
        
        checkResult.scoreCounts.forEach { (score, count) ->
            val prize = when(score) {
                11 -> GameConstants.PRIZE_11
                12 -> GameConstants.PRIZE_12
                13 -> GameConstants.PRIZE_13
                14 -> GameConstants.PRIZE_14
                15 -> GameConstants.PRIZE_15
                else -> BigDecimal.ZERO
            }
            revenue = revenue.add(prize.multiply(BigDecimal(count)))
        }
        
        val profit = revenue.subtract(cost)
        val roi = if (cost.compareTo(BigDecimal.ZERO) > 0) {
            profit.divide(cost, 4, java.math.RoundingMode.HALF_UP).toFloat() * 100
        } else 0f
        
        val isApproximate = !hasHistoricalData
        val disclaimer = if (isApproximate) {
            "Projeção aproximada baseada em valores médios estimados. " +
            "Valores reais podem variar conforme prêmios dos concursos analisados."
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
