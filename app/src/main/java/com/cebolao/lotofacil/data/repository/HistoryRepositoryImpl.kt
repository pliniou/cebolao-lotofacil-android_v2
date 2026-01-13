package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.cache.DrawLruCache
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.mapper.toDraw
import com.cebolao.lotofacil.data.mapper.toDrawDetails
import com.cebolao.lotofacil.data.mapper.toDrawDetailsEntity
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.util.toAppError
import com.cebolao.lotofacil.domain.exception.SyncException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRepository"
private const val API_RESULT_FRESHNESS_MS = 10 * 60 * 1000L

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val syncManager: SyncManager,
    private val logger: Logger,
    @param:ApplicationScope private val scope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryRepository {

    // LRU cache for frequently accessed draws
    private val drawCache = DrawLruCache(maxSize = 100)

    // Session-only cache: valid for a short freshness window and cleared on sync failure.
    @Volatile
    private var latestApiResult: LotofacilApiResult? = null
    private var latestApiResultTimestamp: Long? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val syncMutex = Mutex()
    private var syncDeferred: Deferred<Result<Unit>>? = null
    private var runningSyncJob: Job? = null

    override fun observeHistory(): Flow<List<Draw>> =
        localDataSource.observeLocalHistory()

    override fun observeLastDraw(): Flow<Draw?> = 
        localDataSource.observeLastDraw()

    override suspend fun getHistory(): List<Draw> {
        // Always fetch full history from database for statistics accuracy.
        // The LRU cache only holds a subset (e.g., 100), so returning it
        // would result in incorrect statistics calculation.
        val draws = localDataSource.getLocalHistory()
        
        // Optimistically update cache with recent ones (last 100)
        // We reverse or takeLast depending on sort order, but cache logic handles eviction.
        // To avoid thrashing, we could only put the last N, but putAll handles it.
        // For efficiency, let's just put the top 100 if the list is large.
        if (draws.isNotEmpty()) {
            val toCache = draws.take(100) // Assuming draws are sorted DESC (newest first)
            drawCache.putAll(toCache)
        }
        
        return draws
    }

    override suspend fun getLastDraw(): Draw? {
        // Try from API cache when fresh (session-only validity window).
        val fromApi = latestApiResult?.toDraw()
        if (fromApi != null && isLatestApiResultFresh()) {
            // Update cache
            drawCache.put(fromApi.contestNumber, fromApi)
            return fromApi
        }

        // Prefer local DB when API cache is stale or unavailable.
        val lastDraw = localDataSource.getLastDraw()
        if (lastDraw != null) {
            drawCache.put(lastDraw.contestNumber, lastDraw)
            return lastDraw
        }

        // Fallback to memory cache
        val draws = drawCache.getAll()
        val lastFromCache = draws.maxByOrNull { it.contestNumber }
        if (lastFromCache != null) {
            logger.debug(TAG, "Returning last draw from cache: ${lastFromCache.contestNumber}")
            return lastFromCache
        }

        return null
    }

    override suspend fun getLastDrawDetails(): DrawDetails? {
        val lastDraw = localDataSource.getLastDraw() ?: return null
        
        // 1. Try local DB
        val cachedDetails = localDataSource.getDrawDetails(lastDraw.contestNumber)
        if (cachedDetails != null) {
             return cachedDetails.toDrawDetails(lastDraw)
        }

        // 2. Try API result cache
        return latestApiResult?.let { 
             val entity = it.toDrawDetailsEntity()
             localDataSource.saveDrawDetails(entity)
             entity.toDrawDetails(lastDraw) 
        }
    }

    override fun syncHistory(): Job = synchronized(this) {
        runningSyncJob?.takeIf { it.isActive }?.let { return it }

        val job = scope.launch {
            syncHistoryIfNeeded()
        }
        runningSyncJob = job
        return job
    }

    override suspend fun syncHistoryIfNeeded(): Result<Unit> {
        val deferred = syncMutex.withLock {
            val inFlight = syncDeferred?.takeIf { it.isActive }
            if (inFlight != null) {
                logger.debug(TAG, "Sync already in flight, joining existing work")
                return@withLock inFlight
            }

            val newDeferred = scope.async {
                runSyncIfNeeded()
            }
            syncDeferred = newDeferred
            newDeferred.invokeOnCompletion {
                scope.launch {
                    syncMutex.withLock {
                        if (syncDeferred === newDeferred) {
                            syncDeferred = null
                        }
                    }
                }
            }
            newDeferred
        }

        return deferred.await()
    }

    private suspend fun runSyncIfNeeded(): Result<Unit> {
        val shouldSync = try {
            syncManager.shouldSync()
        } catch (e: IOException) {
            logger.error(TAG, "Network error checking sync status", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            return Result.failure(e)
        } catch (e: SyncException) {
            logger.error(TAG, "Sync service error checking sync status", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            return Result.failure(e)
        }

        if (!shouldSync) {
            logger.info(TAG, "Sync not needed, skipping")
            return Result.success(Unit)
        }

        return try {
            _syncStatus.value = SyncStatus.Syncing
            logger.info(TAG, "Starting history sync")

            // Use SyncManager for sync logic
            latestApiResult = syncManager.performIncrementalSync()
            latestApiResultTimestamp = System.currentTimeMillis()
            
            // Clear cache after successful sync to force refresh
            drawCache.clear()
            logger.debug(TAG, "Cache cleared after sync")

            _syncStatus.value = SyncStatus.Success
            logger.info(TAG, "Sync completed successfully")
            Result.success(Unit)
        } catch (e: IOException) {
            latestApiResult = null
            latestApiResultTimestamp = null
            logger.error(TAG, "Network sync failed", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            Result.failure(e)
        } catch (e: SyncException) {
            latestApiResult = null
            latestApiResultTimestamp = null
            logger.error(TAG, "Sync service error", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        }
    }

    private fun isLatestApiResultFresh(now: Long = System.currentTimeMillis()): Boolean {
        val timestamp = latestApiResultTimestamp ?: return false
        return now - timestamp <= API_RESULT_FRESHNESS_MS
    }
}
