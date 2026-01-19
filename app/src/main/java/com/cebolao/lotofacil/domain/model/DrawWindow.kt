package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Representa a janela de concursos analisada em uma conferência
 */
@Immutable
@Serializable
data class DrawWindow(
    val firstContest: Int,
    val lastContest: Int,
    val totalDraws: Int
)
