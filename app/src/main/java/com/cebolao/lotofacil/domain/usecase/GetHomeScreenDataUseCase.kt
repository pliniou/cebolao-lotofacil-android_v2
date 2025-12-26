package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.util.toAppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

private const val TAG = "GetHomeScreenDataUC"

class GetHomeScreenDataUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val checkGameUseCase: CheckGameUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<AppResult<HomeScreenData>> =
        historyRepository.observeLastDraw()
            .distinctUntilChangedBy { it?.contestNumber }
            .mapLatest { lastDraw ->
                try {
                    if (lastDraw == null) return@mapLatest AppResult.Failure(AppError.Unknown(null))

                    coroutineScope {
                        val statsDeferred = async { getAnalyzedStatsUseCase(timeWindow = 0).getOrThrow() }
                        val detailsDeferred = async { historyRepository.getLastDrawDetails() }
                        val checkDeferred = async {
                            // Flow de 1 emissão -> coleta direta
                            checkGameUseCase(lastDraw.numbers).first().getOrNull()
                        }

                        val stats = statsDeferred.await()
                        val details = detailsDeferred.await()
                        val lastDrawCheckResult = checkDeferred.await()

                        AppResult.Success(
                            HomeScreenData(
                                lastDraw = lastDraw,
                                initialStats = stats,
                                details = details,
                                lastDrawCheckResult = lastDrawCheckResult
                            )
                        )
                    }
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Error generating home data", e)
                    AppResult.Failure(e.toAppError())
                }
            }
            .onStart {
                // Warm cache sem atrapalhar cancelamento
                try {
                    historyRepository.getLastDraw()
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Fail to warm cache", e)
                }
            }
            .flowOn(defaultDispatcher)
}
