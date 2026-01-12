package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.util.toAppError
import javax.inject.Inject

/**
 * Caso de uso dedicado para iniciar a sincronização do histórico de sorteios.
 * Abstrai a chamada ao repositório, facilitando testes e manutenibilidade.
 */
class SyncHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return historyRepository.syncHistoryIfNeeded()
            .fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { AppResult.Failure(it.toAppError()) }
            )
    }
}
