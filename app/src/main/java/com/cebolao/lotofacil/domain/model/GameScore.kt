package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class ScoreStatus {
    EXCELLENT,
    GOOD,
    WARNING,
    BAD
}

@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class MetricEvaluation(
    val name: String,
    val value: Int,
    val status: ScoreStatus,
    val message: String
)

@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class GameScore(
    val totalScore: Int,
    val status: ScoreStatus,
    val evaluations: List<MetricEvaluation>
)
