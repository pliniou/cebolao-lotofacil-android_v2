package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.repository.DatabaseLoadingState
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * UseCase para observar o estado de carregamento do banco de dados.
 * Fornece feedback sobre o progresso da carga inicial de dados.
 */
class ObserveDatabaseLoadingUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): StateFlow<DatabaseLoadingState> = historyRepository.loadingState
}
