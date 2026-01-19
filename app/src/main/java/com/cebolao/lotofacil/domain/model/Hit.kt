package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Representa um hit (acerto) em um concurso específico
 */
@Immutable
@Serializable
data class Hit(
    val contestNumber: Int,
    val score: Int
)
