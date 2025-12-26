package com.cebolao.lotofacil.domain.model

data class GameAnalysisResult(
    val game: LotofacilGame,
    val score: Int,
    val matchedNumbers: List<Int>,
    val lastHitContest: Int?,
    val metrics: GameMetrics,
    val checkReport: CheckReport? = null
)
