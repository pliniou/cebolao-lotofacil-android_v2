package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.util.toAppError
import com.cebolao.lotofacil.domain.exception.SyncException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private const val TAG = "GetLastDrawUseCase"

/**
 * Encapsula a lógica de negócio para obter o último sorteio do histórico.
 * Garante que a ViewModel não acesse o repositório diretamente, seguindo a Clean Architecture.
 */
class GetLastDrawUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val logger: Logger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Busca o último sorteio.
     * @return AppResult.Success(Draw?) se encontrado (inclui null), AppResult.Failure(AppError) se erro.
     */
    suspend operator fun invoke(): AppResult<Draw?> = withContext(ioDispatcher) {
        try {
            AppResult.Success(historyRepository.getLastDraw())
        } catch (e: IOException) {
            logger.error(TAG, "Network error getting last draw", e)
            AppResult.Failure(e.toAppError())
        } catch (e: SyncException) {
            logger.error(TAG, "Sync error getting last draw", e)
            AppResult.Failure(e.toAppError())
        }
    }
}
