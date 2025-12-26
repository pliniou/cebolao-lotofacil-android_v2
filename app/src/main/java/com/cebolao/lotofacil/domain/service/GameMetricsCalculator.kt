package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.GameStatisticsProvider

/** Calculadora que centraliza e garante semântica consistente das métricas de jogo */
class GameMetricsCalculator {
    fun calculate(provider: GameStatisticsProvider, lastDraw: Set<Int>? = null): GameComputedMetrics {
        val repeated = lastDraw?.let { provider.repeatedFrom(it) } ?: 0
        return GameComputedMetrics(
            sum = provider.sum,
            evens = provider.evens,
            primes = provider.primes,
            fibonacci = provider.fibonacci,
            frame = provider.frame,

            sequences = provider.sequences,
            multiplesOf3 = provider.multiplesOf3,
            center = provider.center,
            repeated = repeated
        )
    }
}
