package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Representa um hit (acerto) em um concurso específico
 */
@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class Hit(
    val contestNumber: Int,
    val score: Int
)
