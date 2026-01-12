package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.FilterType

/**
 * Telemetria detalhada da geração de jogos para auditabilidade, debug e análise de performance.
 * 
 * @property seed Random seed usado na geração
 * @property strategy Estratégia final utilizada (pode mudar de HEURISTIC para FALLBACK)
 * @property durationMs Duração total da geração em milissegundos
 * @property totalAttempts Total de tentativas de geração (inclui sucessos e falhas)
 * @property successfulGames Número de jogos gerados com sucesso
 * @property rejectionsByFilter Mapa de rejeições por tipo de filtro
 */
data class GenerationTelemetry(
    val seed: Long,
    val strategy: GenerationStep,
    val durationMs: Long,
    val totalAttempts: Int,
    val successfulGames: Int = 0,
    val rejectionsByFilter: Map<FilterType, Int> = emptyMap()
) {
    /**
     * Taxa de sucesso (0.0 a 1.0)
     */
    val successRate: Float
        get() = if (totalAttempts > 0) successfulGames.toFloat() / totalAttempts else 0f
    
    /**
     * Taxa de rejeição (0.0 a 1.0)
     */
    val rejectionRate: Float
        get() = 1f - successRate
    
    /**
     * Tempo médio por jogo gerado com sucesso (em ms)
     */
    val avgTimePerGame: Long
        get() = if (successfulGames > 0) durationMs / successfulGames else 0L
    
    /**
     * Total de rejeições
     */
    val totalRejections: Int
        get() = rejectionsByFilter.values.sum()
    
    /**
     * Filtro mais restritivo (que mais rejeitou jogos)
     */
    val mostRestrictiveFilter: FilterType?
        get() = rejectionsByFilter.maxByOrNull { it.value }?.key
    
    companion object {
        fun start(): GenerationTelemetry {
            return GenerationTelemetry(
                seed = System.currentTimeMillis(),
                strategy = GenerationStep.HEURISTIC_START,
                durationMs = 0,
                totalAttempts = 0,
                successfulGames = 0,
                rejectionsByFilter = emptyMap()
            )
        }
    }
}
