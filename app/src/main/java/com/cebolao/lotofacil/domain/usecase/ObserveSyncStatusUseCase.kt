package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Novo UseCase para observar o status de sincronização.
 * Remove a necessidade da ViewModel acessar o Repository diretamente.
 */
class ObserveSyncStatusUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): StateFlow<SyncStatus> = historyRepository.syncStatus
}
