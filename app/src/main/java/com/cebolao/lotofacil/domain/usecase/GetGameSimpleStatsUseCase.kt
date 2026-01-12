package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.GameMetrics
import javax.inject.Inject

/**
 * Caso de uso síncrono que retorna métricas numéricas básicas de um jogo.
 */
class GetGameSimpleStatsUseCase @Inject constructor() {
    operator fun invoke(provider: com.cebolao.lotofacil.domain.model.GameStatisticsProvider): GameMetrics {
        if (provider.numbers.isEmpty()) return GameMetrics(averageHit = 0.0, maxHit = 0, minHit = 0, totalHits = 0)

        val nums = provider.numbers.toList()
        val average = if (nums.isEmpty()) 0.0 else nums.map { it.toDouble() }.average()
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
