package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Relatório completo de uma conferência de jogo, incluindo metadados de auditoria
 */
@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class CheckReport(
    val ticket: LotofacilGame,
    val drawWindow: DrawWindow,
    val hits: List<Hit>,
    val financialMetrics: FinancialProjection,
    val timestamp: Long,
    val sourceHash: String
)
