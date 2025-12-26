package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.FilterType

/** Telemetria da geração de jogos para auditabilidade e debug */
data class GenerationTelemetry(
    val seed: Long,
    val strategy: GenerationStep,
    val durationMs: Long,
    val totalAttempts: Int,
    val rejectionsByFilter: Map<FilterType, Int>
)
