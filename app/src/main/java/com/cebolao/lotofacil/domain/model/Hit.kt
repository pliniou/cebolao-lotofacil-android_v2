package com.cebolao.lotofacil.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa um hit (acerto) em um concurso específico
 */
@Serializable
data class Hit(
    val contestNumber: Int,
    val score: Int
)
