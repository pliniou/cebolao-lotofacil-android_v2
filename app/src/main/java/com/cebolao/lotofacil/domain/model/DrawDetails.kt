package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

/**
 * Detalhes estendidos de um sorteio, incluindo premiações e dados do próximo concurso.
 */
data class DrawDetails(
    val draw: Draw,
    val nextEstimatedPrize: Double,
    val nextContestDate: String?,
    val nextContestNumber: Int?,
    val accumulatedValue05: Double,
    val accumulatedValueSpecial: Double,
    val location: String?,
    val winnersByState: List<WinnerLocation>,
    val prizeRates: List<PrizeRate>
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WinnerLocation(
    val state: String,
    val city: String,
    val count: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PrizeRate(
    val description: String,
    val winnerCount: Int,
    val prizeValue: Double
)
