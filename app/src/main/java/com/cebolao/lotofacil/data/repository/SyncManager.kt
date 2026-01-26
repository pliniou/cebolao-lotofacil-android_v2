package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.local.db.DrawDao
import com.cebolao.lotofacil.data.mapper.toValidatedDrawOrNull
import com.cebolao.lotofacil.data.network.ApiService
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.exception.SyncException
import com.cebolao.lotofacil.data.mapper.toEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages synchronization between local and remote data sources.
 * Extracted from HistoryRepositoryImpl for better separation of concerns.
 */
@Singleton
class SyncManager @Inject constructor(
    private val drawDao: DrawDao,
    private val apiService: ApiService,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "SyncManager"
        const val CHUNK_SIZE = 20
        const val MAX_CONCURRENT_REQUESTS = 8
    }

    /**
     * Performs incremental sync from remote to local database.
     * Only fetches new contests that aren't in local storage.
     * 
     * @return Latest API result or null if sync failed
     * @throws AppError if sync encounters unrecoverable error
     */
    suspend fun performIncrementalSync(): LotofacilApiResult? {
        return withContext(ioDispatcher) {
            try {
                // Get current max contest number from local database
                val currentMax = drawDao.getLastDrawSnapshot()?.contestNumber ?: 0

                // Fetch latest draw from remote
                val remoteResult = apiService.getLatestResult()
                    ?: throw SyncException("Unable to fetch latest draw from API")
                val latestDraw = remoteResult.toValidatedDrawOrNull()
                    ?: throw SyncException("Latest draw payload missing or invalid numbers")

                val remoteMax = latestDraw.contestNumber

                Log.d(TAG, "Sync check: local=$currentMax, remote=$remoteMax")

                // No new data available
                if (remoteMax <= currentMax) {
                    Log.i(TAG, "Already up to date")
                    return@withContext remoteResult
                }

                // Calculate range to fetch
                val totalRange = (currentMax + 1)..remoteMax
                Log.i(TAG, "Syncing ${totalRange.count()} new contests")

                // Fetch in chunks to avoid overwhelming network/database
                val chunks = totalRange.chunked(CHUNK_SIZE)

                for ((index, chunk) in chunks.withIndex()) {
                    if (chunk.isEmpty()) continue

                    val chunkRange = chunk.first()..chunk.last()
                    Log.d(TAG, "Fetching chunk ${index + 1}/${chunks.size}: $chunkRange")

                    val newDraws = fetchDrawsInRange(chunkRange)

                    if (newDraws.isNotEmpty()) {
                        drawDao.insertAll(newDraws.map { it.toEntity() })
                        Log.d(TAG, "Saved ${newDraws.size} draws from chunk")
                    }
                }

                Log.i(TAG, "Sync completed successfully")
                remoteResult

            } catch (e: IOException) {
                Log.e(TAG, "Network error during sync", e)
                throw SyncException("Network sync failed", e)
            } catch (e: SerializationException) {
                Log.e(TAG, "Invalid response format during sync", e)
                throw SyncException("Invalid response format", e)
            } catch (e: CancellationException) {
                throw e // Don't catch cancellation
            }
        }
    }

    /**
     * Checks if a sync is needed by comparing local and remote latest contests.
     */
    suspend fun shouldSync(): Boolean {
        return withContext(ioDispatcher) {
            try {
                val currentMax = drawDao.getLastDrawSnapshot()?.contestNumber ?: 0
                val remoteResult = apiService.getLatestResult()
                    ?: throw SyncException("Unable to fetch latest draw from API")
                val latestDraw = remoteResult.toValidatedDrawOrNull()
                    ?: throw SyncException("Latest draw payload missing or invalid numbers")

                val remoteMax = latestDraw.contestNumber
                remoteMax > currentMax
            } catch (e: IOException) {
                Log.w(TAG, "Network error checking sync status", e)
                throw SyncException("Network check failed", e)
            } catch (e: SerializationException) {
                Log.w(TAG, "Invalid response format checking sync status", e)
                throw SyncException("Invalid response format", e)
            }
        }
    }

    // Helper method to fetch draws in range
    private suspend fun fetchDrawsInRange(range: IntRange): List<com.cebolao.lotofacil.domain.model.Draw> {
        if (range.isEmpty()) return emptyList()

        val first = range.first
        val size = (range.last - range.first + 1).coerceAtLeast(0)
        if (size == 0) return emptyList()

        val output = arrayOfNulls<com.cebolao.lotofacil.domain.model.Draw>(size)
        val nextIndex = java.util.concurrent.atomic.AtomicInteger(0)
        val workers = kotlin.math.min(MAX_CONCURRENT_REQUESTS, size)

        coroutineScope {
            List(workers) {
                async {
                    while (true) {
                        val idx = nextIndex.getAndIncrement()
                        if (idx >= size) break

                        val contestNumber = first + idx
                        val draw = fetchSingleDraw(contestNumber)
                        output[idx] = draw
                    }
                }
            }.awaitAll()
        }

        val draws = output.filterNotNull()
        if (draws.size != size) {
            throw SyncException("Incomplete draw payloads for range $range (expected $size, got ${draws.size})")
        }
        return draws
    }

    private suspend fun fetchSingleDraw(contestNumber: Int): com.cebolao.lotofacil.domain.model.Draw? {
        return withContext(ioDispatcher) {
            runCatching {
                val apiResult = apiService.getResultByContest(contestNumber)
                val validated = apiResult?.toValidatedDrawOrNull()
                if (validated == null) {
                    Log.w(TAG, "Invalid draw payload for contest $contestNumber")
                }
                validated
            }.getOrNull()
        }
    }
}
