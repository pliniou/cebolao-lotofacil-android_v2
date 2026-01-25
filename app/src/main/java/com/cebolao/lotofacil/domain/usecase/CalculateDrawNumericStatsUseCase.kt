package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.GameMetrics
import com.cebolao.lotofacil.domain.model.GameStatisticsProvider
import javax.inject.Inject

/**
 * Caso de uso síncrono que calcula métricas numéricas básicas (média, min, max) dos números de um jogo/sorteio.
 * Renomeado de GetGameSimpleStatsUseCase para evitar ambiguidade com estatísticas de sorteio.
 */
class CalculateDrawNumericStatsUseCase @Inject constructor() {
    operator fun invoke(provider: GameStatisticsProvider): GameMetrics {
        if (provider.numbers.isEmpty()) return GameMetrics(averageHit = 0.0, maxHit = 0, minHit = 0, totalHits = 0)

        val nums = provider.numbers.toList()
        val average = if (nums.isEmpty()) 0.0 else nums.average()
        val max = nums.maxOrNull() ?: 0
        val min = nums.minOrNull() ?: 0
        val total = nums.size

        return GameMetrics(
            averageHit = average,
            maxHit = max,
            minHit = min,
            totalHits = total
        )
    }
}
