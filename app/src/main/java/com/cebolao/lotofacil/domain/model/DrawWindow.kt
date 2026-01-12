package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Representa a janela de concursos analisada em uma conferência
 */
@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class DrawWindow(
    val firstContest: Int,
    val lastContest: Int,
    val totalDraws: Int
)
