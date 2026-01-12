package com.cebolao.lotofacil.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "HistorySyncWorker"

/**
 * Fase 2: orquestra o sync do histórico via WorkManager (sem disparar rede no init do repository).
 */
@HiltWorker
class HistorySyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = runCatching {
        historyRepository.syncHistory().join()
        when (val status = historyRepository.syncStatus.value) {
            is SyncStatus.Success,
            is SyncStatus.Idle -> Result.success()
            is SyncStatus.Syncing -> if (runAttemptCount < 3) Result.retry() else Result.failure()
            is SyncStatus.Failed -> {
                Log.e(TAG, "History sync failed: ${status.error}")
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }.getOrElse { e ->
        Log.e(TAG, "History sync failed", e)
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }
}
