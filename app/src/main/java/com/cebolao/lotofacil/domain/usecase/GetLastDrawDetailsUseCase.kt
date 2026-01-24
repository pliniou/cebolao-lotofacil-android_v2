package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import javax.inject.Inject

/** Fetches details for the last draw. */
class GetLastDrawDetailsUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(): AppResult<DrawDetails?> =
        historyRepository.getLastDrawDetails()
}
