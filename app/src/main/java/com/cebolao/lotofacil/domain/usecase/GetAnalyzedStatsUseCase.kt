package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.StatisticsReport
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAnalyzedStatsUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(timeWindow: Int = 0): AppResult<StatisticsReport> = withContext(dispatcher) {
        try {
            require(timeWindow >= 0) { "timeWindow must be >= 0" }
            val history = when (val historyResult = historyRepository.getHistory()) {
                is AppResult.Failure -> return@withContext historyResult
                is AppResult.Success -> historyResult.value
            }
            val report = statisticsAnalyzer.analyze(history, timeWindow)
            AppResult.Success(report)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }
}
