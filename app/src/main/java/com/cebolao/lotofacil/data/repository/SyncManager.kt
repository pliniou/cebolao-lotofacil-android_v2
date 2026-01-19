package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.domain.exception.SyncException
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
    private val localDataSource: HistoryLocalDataSource,
    private val remoteDataSource: HistoryRemoteDataSource,
    private val logger: Logger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private companion object {
        const val TAG = "SyncManager"
        const val CHUNK_SIZE = 20
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
                val currentMax = localDataSource.getLastDraw()?.contestNumber ?: 0

                // Fetch latest draw from remote
                val remoteResult = remoteDataSource.getLatestDraw()
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

                    val newDraws = remoteDataSource.getDrawsInRange(chunkRange)

                    if (newDraws.isNotEmpty()) {
                        localDataSource.saveNewContests(newDraws)
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
                val currentMax = localDataSource.getLastDraw()?.contestNumber ?: 0
                val remoteResult = remoteDataSource.getLatestDraw() ?: return@withContext true

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
}

