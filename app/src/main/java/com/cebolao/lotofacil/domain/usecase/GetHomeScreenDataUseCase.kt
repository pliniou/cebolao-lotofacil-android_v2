package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.util.toAppError
import com.cebolao.lotofacil.domain.exception.SyncException
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
import java.io.IOException
import javax.inject.Inject

private const val TAG = "GetHomeScreenDataUC"

class GetHomeScreenDataUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,

    private val checkGameUseCase: CheckGameUseCase,
    private val logger: Logger,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<AppResult<HomeScreenData>> =
        historyRepository.observeLastDraw()
            .distinctUntilChangedBy { it?.contestNumber }
            .mapLatest { lastDraw ->
                try {
                    if (lastDraw == null) return@mapLatest AppResult.Failure(AppError.Unknown(null))

                    coroutineScope {
                        val detailsDeferred = async { historyRepository.getLastDrawDetails() }
                        val checkDeferred = async {
                            // Flow de 1 emissão -> coleta direta
                            val checkResult = checkGameUseCase(lastDraw.numbers).first()
                            if (checkResult is AppResult.Success) checkResult.value else null
                        }

                        val details = detailsDeferred.await()
                        val lastDrawCheckResult = checkDeferred.await()

                        AppResult.Success(
                            HomeScreenData(
                                lastDraw = lastDraw,
                                details = details,
                                lastDrawCheckResult = lastDrawCheckResult
                            )
                        )
                    }
                } catch (e: IOException) {
                    logger.error(TAG, "Network error generating home data", e)
                    AppResult.Failure(e.toAppError())
                } catch (e: SyncException) {
                    logger.error(TAG, "Sync error generating home data", e)
                    AppResult.Failure(e.toAppError())
                } catch (e: CancellationException) {
                    throw e
                }
            }
            .onStart {
                // Warm cache sem atrapalhar cancelamento
                try {
                    historyRepository.getLastDraw()
                } catch (e: IOException) {
                    logger.error(TAG, "Network error warming cache", e)
                } catch (e: SyncException) {
                    logger.error(TAG, "Sync error warming cache", e)
                } catch (e: CancellationException) {
                    throw e
                }
            }
            .flowOn(defaultDispatcher)
}
