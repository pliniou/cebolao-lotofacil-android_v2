package com.cebolao.lotofacil.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.domain.repository.HistoryRepository
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
        Result.success()
    }.getOrElse { e ->
        Log.e(TAG, "History sync failed", e)
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }
}
