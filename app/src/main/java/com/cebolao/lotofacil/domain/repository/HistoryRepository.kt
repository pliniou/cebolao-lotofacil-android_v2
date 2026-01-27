package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.DatabaseLoadingState
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
     * Current status of history synchronization process.
     */
    val syncStatus: StateFlow<SyncStatus>

    /**
     * Current status of database loading process.
     */
    val loadingState: StateFlow<DatabaseLoadingState>

    /**
     * Initiates history synchronization (implementation should be idempotent when possible).
     */
    fun syncHistory(): Job

    /**
     * Executes history synchronization if needed and returns the result.
     */
    suspend fun syncHistoryIfNeeded(): AppResult<Unit>

    /**
     * Observes the complete list of contests.
     */
    fun observeHistory(): Flow<List<Draw>>

    /**
     * Observes only the last contest. Optimized for Home Screen.
     */
    fun observeLastDraw(): Flow<Draw?>

    /**
     * Returns the complete list of contests available locally.
     */
    suspend fun getHistory(): AppResult<List<Draw>>

    /**
     * Returns the last contest or null.
     */
    suspend fun getLastDraw(): AppResult<Draw?>

    /**
     * Returns details of the last contest, when available.
     */
    suspend fun getLastDrawDetails(): AppResult<DrawDetails?>
}
