package com.cebolao.lotofacil.mapper

import com.cebolao.lotofacil.domain.model.GameMetrics
import com.cebolao.lotofacil.ui.model.toUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale

typealias DomainGameAnalysisResult = com.cebolao.lotofacil.domain.model.GameAnalysisResult
typealias UiGameAnalysisResult = com.cebolao.lotofacil.ui.model.UiGameAnalysisResult

/**
 * Transforms GameMetrics into a list of key-value pairs for display in the UI.
 */
fun GameMetrics.toSimpleStats(): ImmutableList<Pair<String, String>> {
    return listOf(
        "Média de acertos" to String.format(Locale("pt", "BR"), "%.2f", averageHit),
        "Máximo de acertos" to "$maxHit",
        "Mínimo de acertos" to "$minHit",
        "Total de acertos" to "$totalHits"
    ).toImmutableList()
}

/**
 * Maps a domain GameAnalysisResult to its corresponding UI model representation.
 *
 * @throws IllegalArgumentException if checkReport is null.
 */
fun DomainGameAnalysisResult.toUiModel(): UiGameAnalysisResult {
    val report = requireNotNull(checkReport) { "checkReport cannot be null for UI display" }

    return UiGameAnalysisResult(
        game = this.game.toUiModel(),
        simpleStats = this.metrics.toSimpleStats(),
        checkReport = report
    )
}
