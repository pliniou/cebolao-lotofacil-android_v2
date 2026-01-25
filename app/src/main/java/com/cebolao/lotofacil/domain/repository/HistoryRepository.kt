package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.data.repository.DatabaseLoadingState
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data object Success : SyncStatus
    data class Failed(val error: AppError) : SyncStatus
}

interface HistoryRepository {
    /**
     * Estado atual do processo de sincronização do histórico.
     */
    val syncStatus: StateFlow<SyncStatus>

    /**
     * Estado atual do processo de carregamento do banco de dados.
     */
    val loadingState: StateFlow<DatabaseLoadingState>

    /**
     * Inicia sincronização do histórico (implementação deve ser idempotente quando possível).
     */
    fun syncHistory(): Job

    /**
     * Executa sincronização do histórico caso necessário e retorna o resultado.
     */
    suspend fun syncHistoryIfNeeded(): AppResult<Unit>

    /**
     * Observa toda a lista de concursos.
     */
    fun observeHistory(): Flow<List<Draw>>

    /**
     * Observa apenas o último concurso. Otimizado para Home Screen.
     */
    fun observeLastDraw(): Flow<Draw?>

    /**
     * Retorna a lista completa de concursos disponíveis localmente.
     */
    suspend fun getHistory(): AppResult<List<Draw>>

    /**
     * Retorna o último concurso ou null.
     */
    suspend fun getLastDraw(): AppResult<Draw?>

    /**
     * Retorna detalhes do último concurso, quando disponível.
     */
    suspend fun getLastDrawDetails(): AppResult<DrawDetails?>
}
