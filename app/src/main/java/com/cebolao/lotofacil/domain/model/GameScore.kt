package com.cebolao.lotofacil.domain.model

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

@Immutable
@Serializable
data class MetricEvaluation(
    val name: String,
    val value: Int,
    val status: ScoreStatus,
    val message: String
)

@Immutable
@Serializable
data class GameScore(
    val totalScore: Int,
    val status: ScoreStatus,
    val evaluations: List<MetricEvaluation>
)
