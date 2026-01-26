package com.cebolao.lotofacil.domain.model
import kotlinx.serialization.Serializable

/**
 * Relatório completo de uma conferência de jogo, incluindo metadados de auditoria
 */
@Serializable
data class CheckReport(
    val ticket: LotofacilGame,
    val drawWindow: DrawWindow,
    val hits: List<Hit>,
    val financialMetrics: FinancialProjection,
    val timestamp: Long,
    val sourceHash: String
)
