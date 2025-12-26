package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "GetLastDrawUseCase"

/**
 * Encapsula a lógica de negócio para obter o último sorteio do histórico.
 * Garante que a ViewModel não acesse o repositório diretamente, seguindo a Clean Architecture.
 */
class GetLastDrawUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Busca o último sorteio.
     * @return Result.success(Draw?) se encontrado (inclui null), Result.failure(Throwable) se erro.
     */
    suspend operator fun invoke(): Result<Draw?> = withContext(ioDispatcher) {
        runCatching {
            historyRepository.getLastDraw()
        }.onFailure { e ->
            Log.e(TAG, "Failed to get last draw", e)
        }
    }
}
