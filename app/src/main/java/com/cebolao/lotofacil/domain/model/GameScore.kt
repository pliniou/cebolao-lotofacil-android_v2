package com.cebolao.lotofacil.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScoreStatus {
    EXCELLENT,
    GOOD,
    WARNING,
    BAD
}

@Serializable
data class MetricEvaluation(
    val name: String,
    val value: Int,
    val status: ScoreStatus,
    val message: String
)

@Serializable
data class GameScore(
    val totalScore: Int,
    val status: ScoreStatus,
    val evaluations: List<MetricEvaluation>
)
