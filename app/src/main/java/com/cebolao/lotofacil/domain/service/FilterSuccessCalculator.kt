package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.FilterState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

@Singleton
class FilterSuccessCalculator @Inject constructor() {

    private companion object {
        const val MIN_RANGE_FACTOR = 0.05f
        const val MIN_PROBABILITY_FLOOR = 0.0001f
        const val MAX_PROBABILITY = 1.0f
    }

    /**
     * Estima uma "taxa de sucesso" agregada dos filtros ativos.
     *
     * Implementação usa média geométrica no espaço log para estabilidade numérica.
     * Mantém comportamento anterior, mas:
     * - filtra internamente apenas filtros habilitados (resiliente a chamadas com a lista completa),
     * - reduz alocações intermediárias.
     */
    operator fun invoke(filters: List<FilterState>): Float {
        val activeFilters = filters.filter { it.isEnabled }
        if (activeFilters.isEmpty()) return MAX_PROBABILITY

        var logSum = 0.0
        for (filter in activeFilters) {
            val rangeFactor = max(filter.rangePercentage, MIN_RANGE_FACTOR)
            val strength = (filter.type.historicalSuccessRate * rangeFactor)
                .coerceIn(MIN_PROBABILITY_FLOOR, MAX_PROBABILITY)
                .toDouble()

            logSum += ln(strength)
        }

        val geometricMean = exp(logSum / activeFilters.size).toFloat()
        return geometricMean.coerceIn(0f, MAX_PROBABILITY)
    }
}
