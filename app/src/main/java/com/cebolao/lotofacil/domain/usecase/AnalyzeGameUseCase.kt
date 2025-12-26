package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.GameAnalysisResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AnalyzeGameUseCase @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame): Result<GameAnalysisResult> = withContext(dispatcher) {
        runCatching {
            val checkReport = checkGameUseCase(game.numbers).first().getOrThrow()

            val metrics = getGameSimpleStatsUseCase(game)

            // Calculate score (max score in hits or 0)
            val score = checkReport.hits.maxOfOrNull { it.score } ?: 0
            
            // matchedNumbers seems to correspond to recentHits contours in legacy logic?
            // "matchedNumbers" usually means which numbers matched in a specific draw.
            // But recentHits was (Contest, Score). 
            // In legacy code: matchedNumbers = checkResult.recentHits.map { it.first } (List of contest numbers)
            // Let's preserve that logic: List of contest numbers where it hit.
            val matchedNumbers = checkReport.hits.map { it.contestNumber }

            val lastHitContest = checkReport.hits.maxByOrNull { it.contestNumber }?.contestNumber

            GameAnalysisResult(
                game = game,
                score = score,
                matchedNumbers = matchedNumbers,
                lastHitContest = lastHitContest,
                metrics = metrics,
                checkReport = checkReport
            )
        }
    }
}
