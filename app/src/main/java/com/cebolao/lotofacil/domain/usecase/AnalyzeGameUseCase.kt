package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.GameAnalysisResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
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
                        when (val historyResult = historyRepository.getHistory()) {
                            is AppResult.Failure -> emptySet()
                            is AppResult.Success -> historyResult.value.find { it.contestNumber == contest }?.numbers
                                ?: emptySet()
                        }
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
        } catch (e: Exception) {
             AppResult.Failure(AppError.Unknown(e))
        }
    }
}
