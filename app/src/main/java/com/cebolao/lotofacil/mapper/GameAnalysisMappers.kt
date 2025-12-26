package com.cebolao.lotofacil.mapper

import com.cebolao.lotofacil.domain.model.GameMetrics
import kotlinx.collections.immutable.toImmutableList
import java.util.Locale
import com.cebolao.lotofacil.domain.model.GameAnalysisResult as DomainGameAnalysisResult
import com.cebolao.lotofacil.viewmodels.GameAnalysisResult as UiGameAnalysisResult

fun GameMetrics.toSimpleStats(): kotlinx.collections.immutable.ImmutableList<Pair<String, String>> {
    return listOf(
        "Average Hit" to String.format(Locale.US, "%.2f", this.averageHit),
        "Max Hit" to this.maxHit.toString(),
        "Min Hit" to this.minHit.toString(),
        "Total Hits" to this.totalHits.toString()
    ).toImmutableList()
}

fun DomainGameAnalysisResult.toUiModel(): UiGameAnalysisResult {
    val check = requireNotNull(this.checkReport) { "checkReport is required for UI mapping" }
    val simpleStats = this.metrics.toSimpleStats()

    return UiGameAnalysisResult(
        game = this.game,
        simpleStats = simpleStats,
        checkReport = check
    )
}
