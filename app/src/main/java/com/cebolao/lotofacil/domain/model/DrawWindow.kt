package com.cebolao.lotofacil.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa a janela de concursos analisada em uma conferência
 */
@Serializable
data class DrawWindow(
    val firstContest: Int,
    val lastContest: Int,
    val totalDraws: Int
)
