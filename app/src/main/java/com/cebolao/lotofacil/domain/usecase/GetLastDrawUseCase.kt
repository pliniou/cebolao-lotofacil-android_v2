package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Returns a flow of the most recent draw. */
class GetLastDrawUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<AppResult<Draw>> =
        historyRepository.observeLastDraw().map { draw ->
            draw?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.NotFound("last draw not found"))
        }
}
