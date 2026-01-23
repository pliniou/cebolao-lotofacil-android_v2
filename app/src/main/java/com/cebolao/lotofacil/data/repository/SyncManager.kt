package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.local.db.DrawDao
import com.cebolao.lotofacil.data.network.ApiService
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.domain.exception.SyncException
import com.cebolao.lotofacil.data.mapper.toEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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
    private val logger: Logger,
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

                val remoteMax = remoteResult.numero

                logger.debug(TAG, "Sync check: local=$currentMax, remote=$remoteMax")

                // No new data available
                if (remoteMax <= currentMax) {
                    logger.info(TAG, "Already up to date")
                    return@withContext remoteResult
                }

                // Calculate range to fetch
                val totalRange = (currentMax + 1)..remoteMax
                logger.info(TAG, "Syncing ${totalRange.count()} new contests")

                // Fetch in chunks to avoid overwhelming network/database
                val chunks = totalRange.chunked(CHUNK_SIZE)

                for ((index, chunk) in chunks.withIndex()) {
                    if (chunk.isEmpty()) continue

                    val chunkRange = chunk.first()..chunk.last()
                    logger.debug(TAG, "Fetching chunk ${index + 1}/${chunks.size}: $chunkRange")

                    val newDraws = fetchDrawsInRange(chunkRange)

                    if (newDraws.isNotEmpty()) {
                        drawDao.insertAll(newDraws.map { it.toEntity() })
                        logger.debug(TAG, "Saved ${newDraws.size} draws from chunk")
                    }
                }

                logger.info(TAG, "Sync completed successfully")
                remoteResult

            } catch (e: IOException) {
                logger.error(TAG, "Network error during sync", e)
                throw SyncException("Network sync failed", e)
            } catch (e: SerializationException) {
                logger.error(TAG, "Invalid response format during sync", e)
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
                val remoteResult = apiService.getLatestResult() ?: return@withContext true

                val remoteMax = remoteResult.numero
                remoteMax > currentMax
            } catch (e: IOException) {
                logger.warning(TAG, "Network error checking sync status", e)
                throw SyncException("Network check failed", e)
            } catch (e: SerializationException) {
                logger.warning(TAG, "Invalid response format checking sync status", e)
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

        return output.filterNotNull()
    }

    private suspend fun fetchSingleDraw(contestNumber: Int): com.cebolao.lotofacil.domain.model.Draw? {
        return withContext(ioDispatcher) {
            runCatching {
                val apiResult = apiService.getResultByContest(contestNumber)
                apiResultToDraw(apiResult)
            }.getOrNull()
        }
    }

    private fun apiResultToDraw(apiResult: LotofacilApiResult): com.cebolao.lotofacil.domain.model.Draw? {
        return runCatching {
            val contest = apiResult.numero
            val mask = stringsToMask(apiResult.listaDezenas)
            if (contest <= 0 || java.lang.Long.bitCount(mask) != 15) return null

            val dateMillis = apiResult.dataApuracao
                ?.takeIf { it.isNotBlank() }
                ?.let { dateStr ->
                    runCatching {
                        java.time.LocalDate.parse(dateStr, dateFormatter)
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }.getOrNull()
                }

            com.cebolao.lotofacil.domain.model.Draw(contest, mask, dateMillis)
        }.getOrNull()
    }

    private fun stringsToMask(numbers: List<String>): Long {
        var mask = 0L
        for (s in numbers) {
            val n = s.toIntOrNull() ?: continue
            val idx = n - 1
            if (idx in 0..63) mask = mask or (1L shl idx)
        }
        return mask
    }

    private val dateFormatter = java.time.format.DateTimeFormatter
        .ofPattern("dd/MM/yyyy", java.util.Locale.forLanguageTag("pt-BR"))
}

