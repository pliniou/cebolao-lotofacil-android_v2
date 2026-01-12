package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.LotofacilGame

enum class GenerationStep {
    RANDOM_START,
    HEURISTIC_START,
    RANDOM_FALLBACK
}

enum class GenerationFailureReason {
    NO_HISTORY,
    GENERIC_ERROR,
    FILTERS_TOO_STRICT
}

sealed interface GenerationProgressType {
    data object Started : GenerationProgressType
    data class Step(val step: GenerationStep, val current: Int = 0) : GenerationProgressType
    data object Attempt : GenerationProgressType
    data class Finished(
        val games: List<LotofacilGame>,
        val telemetry: GenerationTelemetry? = null
    ) : GenerationProgressType
    data class Failed(val reason: GenerationFailureReason) : GenerationProgressType
}

data class GenerationProgress(
    val current: Int,
    val total: Int,
    val progressType: GenerationProgressType
) {
    // Convenience properties for backward compatibility
    val type: GenerationProgressType get() = progressType
    val count: Int get() = when (val pt = progressType) {
        is GenerationProgressType.Finished -> pt.games.size
        else -> current
    }

    companion object {
        fun started(total: Int) =
            GenerationProgress(0, total, GenerationProgressType.Started)

        fun step(step: GenerationStep, current: Int, total: Int) =
            GenerationProgress(current, total, GenerationProgressType.Step(step, current))

        fun attempt(current: Int, total: Int) =
            GenerationProgress(current, total, GenerationProgressType.Attempt)

        fun finished(games: List<LotofacilGame>, telemetry: GenerationTelemetry? = null) =
            GenerationProgress(games.size, games.size, GenerationProgressType.Finished(games, telemetry))

        fun failed(reason: GenerationFailureReason) =
            GenerationProgress(0, 0, GenerationProgressType.Failed(reason))
    }
}
