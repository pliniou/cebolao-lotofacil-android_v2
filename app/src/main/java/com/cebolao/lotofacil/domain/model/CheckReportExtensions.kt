package com.cebolao.lotofacil.domain.model

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

/**
 * Converte CheckReport para CheckResult para compatibilidade com componentes UI existentes
 */
fun CheckReport.toCheckResult(): CheckResult {
    // Agrupar hits por score para criar scoreCounts
    val scoreCountsMutable = mutableMapOf<Int, Int>()
    hits.forEach { hit ->
        if (hit.score >= com.cebolao.lotofacil.domain.GameConstants.MIN_PRIZE_SCORE) {
            scoreCountsMutable[hit.score] = (scoreCountsMutable[hit.score] ?: 0) + 1
        }
    }
    
    val lastHit = hits.firstOrNull { it.score >= com.cebolao.lotofacil.domain.GameConstants.MIN_PRIZE_SCORE }
    
    val recentHits = hits.map { it.contestNumber to it.score }.toImmutableList()
    
    return CheckResult(
        scoreCounts = scoreCountsMutable.toImmutableMap(),
        lastHitContest = lastHit?.contestNumber,
        lastHitScore = lastHit?.score,
        lastCheckedContest = drawWindow.totalDraws,
        recentHits = recentHits
    )
}
