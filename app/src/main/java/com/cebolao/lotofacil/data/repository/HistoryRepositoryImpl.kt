package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.cache.DrawLruCache
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.mapper.toDraw
import com.cebolao.lotofacil.data.mapper.toDrawDetails
import com.cebolao.lotofacil.data.mapper.toDrawDetailsEntity
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.exception.SyncException
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.util.toAppError
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRepository"
private const val API_RESULT_FRESHNESS_MS = 10 * 60 * 1000L

private data class LatestApiResult(
    val payload: LotofacilApiResult,
    val timestampMs: Long
) {
    fun isFresh(now: Long = System.currentTimeMillis()): Boolean =
        now - timestampMs <= API_RESULT_FRESHNESS_MS
}

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val syncManager: SyncManager,
    private val logger: Logger,
    @param:ApplicationScope private val scope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryRepository {

    private val drawCache = DrawLruCache(maxSize = 100)
    private val latestApiResultRef = AtomicReference<LatestApiResult?>()
    private val historyCacheRef = AtomicReference<List<Draw>?>(null)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val syncMutex = Mutex()
    private var syncDeferred: Deferred<Result<Unit>>? = null
    private var runningSyncJob: Job? = null

    override fun observeHistory(): Flow<List<Draw>> =
        localDataSource.observeLocalHistory()
            .onEach { historyCacheRef.set(it) }

    override fun observeLastDraw(): Flow<Draw?> =
        localDataSource.observeLastDraw()

    override suspend fun getHistory(): List<Draw> {
        historyCacheRef.get()?.let { cached ->
            if (cached.isNotEmpty()) return cached
        }

        val draws = localDataSource.getLocalHistory()

        if (draws.isNotEmpty()) {
            val toCache = draws.take(100)
            drawCache.putAll(toCache)
            historyCacheRef.set(draws)
        }

        return draws
    }

    override suspend fun getLastDraw(): Draw? {
        getFreshApiResult()?.toDraw()?.let { fromApi ->
            drawCache.put(fromApi.contestNumber, fromApi)
            return fromApi
        }

        val lastDraw = localDataSource.getLastDraw()
        if (lastDraw != null) {
            drawCache.put(lastDraw.contestNumber, lastDraw)
            return lastDraw
        }

        val lastFromCache = drawCache.getAll().maxByOrNull { it.contestNumber }
        if (lastFromCache != null) {
            logger.debug(TAG, "Returning last draw from cache: ${lastFromCache.contestNumber}")
            return lastFromCache
        }

        return null
    }

    override suspend fun getLastDrawDetails(): DrawDetails? {
        val lastDraw = localDataSource.getLastDraw() ?: return null

        val cachedDetails = localDataSource.getDrawDetails(lastDraw.contestNumber)
        if (cachedDetails != null) {
            return cachedDetails.toDrawDetails(lastDraw)
        }

        return getFreshApiResult()?.let { result ->
            val entity = result.toDrawDetailsEntity()
            localDataSource.saveDrawDetails(entity)
            entity.toDrawDetails(lastDraw)
        }
    }

    override fun syncHistory(): Job = synchronized(this) {
        runningSyncJob?.takeIf { it.isActive }?.let { return it }

        val job = scope.launch(ioDispatcher) {
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

            val newDeferred = scope.async(ioDispatcher) {
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
            _syncStatus.value = SyncStatus.Success
            return Result.success(Unit)
        }

        return try {
            _syncStatus.value = SyncStatus.Syncing
            logger.info(TAG, "Starting history sync")

            val latestResult = syncManager.performIncrementalSync()
            cacheLatestApiResult(latestResult)

            drawCache.clear()
            historyCacheRef.set(null)
            logger.debug(TAG, "Cache cleared after sync")

            _syncStatus.value = SyncStatus.Success
            logger.info(TAG, "Sync completed successfully")
            Result.success(Unit)
        } catch (e: IOException) {
            cacheLatestApiResult(null)
            logger.error(TAG, "Network sync failed", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            Result.failure(e)
        } catch (e: SyncException) {
            cacheLatestApiResult(null)
            logger.error(TAG, "Sync service error", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        }
    }

    private fun getFreshApiResult(now: Long = System.currentTimeMillis()): LotofacilApiResult? =
        latestApiResultRef.get()?.takeIf { it.isFresh(now) }?.payload

    private fun cacheLatestApiResult(result: LotofacilApiResult?) {
        latestApiResultRef.set(
            result?.let { LatestApiResult(payload = it, timestampMs = System.currentTimeMillis()) }
        )
    }
}
