package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.GameAnalysisResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.util.toAppError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AnalyzeGameUseCase @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    private val historyRepository: HistoryRepository,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame): AppResult<GameAnalysisResult> = withContext(dispatcher) {
        try {
            // Flow de 1 emissão agora retorna AppResult<CheckReport>
            when (val checkResult = checkGameUseCase(game.numbers).first()) {
                is AppResult.Failure -> AppResult.Failure(checkResult.error)
                is AppResult.Success -> {
                    val checkReport = checkResult.value
                    
                    val metrics = getGameSimpleStatsUseCase(game)
                    val score = checkReport.hits.maxOfOrNull { it.score } ?: 0

                    val lastDrawNumbers = checkReport.hits.firstOrNull()?.let { hit ->
                        val contest = hit.contestNumber
                        historyRepository.getHistory().find { it.contestNumber == contest }?.numbers
                    } ?: emptySet()

                    val matchedNumbers = game.numbers.intersect(lastDrawNumbers).toList().sorted()
                    val lastHitContest = checkReport.hits.maxByOrNull { it.contestNumber }?.contestNumber

                    AppResult.Success(
                        GameAnalysisResult(
                            game = game,
                            score = score,
                            matchedNumbers = matchedNumbers,
                            lastHitContest = lastHitContest,
                            metrics = metrics,
                            checkReport = checkReport
                        )
                    )
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
             AppResult.Failure(e.toAppError())
        }
    }
}
