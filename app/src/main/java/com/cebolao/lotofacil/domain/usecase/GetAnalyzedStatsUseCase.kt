package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.StatisticsReport
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAnalyzedStatsUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(timeWindow: Int = 0): Result<StatisticsReport> = withContext(dispatcher) {
        runCatching {
            require(timeWindow >= 0) { "timeWindow must be >= 0" }
            val history = historyRepository.getHistory()
            statisticsAnalyzer.analyze(history, timeWindow)
        }
    }
}
