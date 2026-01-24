package com.cebolao.lotofacil.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "HistorySyncWorker"
private const val MAX_ATTEMPTS = 3

/**
 * Orquestra o sync do historico via WorkManager (sem disparar rede no init do repository).
 */
@HiltWorker
class HistorySyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = runCatching {
        val result = historyRepository.syncHistoryIfNeeded()
        when (result) {
            is AppResult.Success -> Result.success()
            is AppResult.Failure -> {
                Log.e(TAG, "History sync failed (attempt ${runAttemptCount + 1})")
                if (shouldRetry()) Result.retry() else Result.failure()
            }
        }
    }.getOrElse { e ->
        Log.e(TAG, "History sync failed (attempt ${runAttemptCount + 1})", e)
        if (shouldRetry()) Result.retry() else Result.failure()
    }

    private fun shouldRetry(): Boolean = runAttemptCount + 1 < MAX_ATTEMPTS
}
