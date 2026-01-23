package com.cebolao.lotofacil.data.repository

import android.content.Context
import com.cebolao.lotofacil.data.cache.DrawLruCache
import com.cebolao.lotofacil.data.local.db.AppDatabase
import com.cebolao.lotofacil.data.local.db.DrawDao
import com.cebolao.lotofacil.data.local.db.DrawDetailsDao
import com.cebolao.lotofacil.data.local.db.DrawDetailsEntity
import com.cebolao.lotofacil.data.mapper.toDraw
import com.cebolao.lotofacil.data.mapper.toDrawDetails
import com.cebolao.lotofacil.data.mapper.toDrawDetailsEntity
import com.cebolao.lotofacil.data.mapper.toEntity
import com.cebolao.lotofacil.data.network.ApiService
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.data.util.HistoryParser
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import androidx.room.withTransaction

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
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val drawDao: DrawDao,
    private val drawDetailsDao: DrawDetailsDao,
    private val apiService: ApiService,
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
        drawDao.getAllDraws()
            .onStart { runBlocking { ensureInitialized() } }
            .map { entities: List<com.cebolao.lotofacil.data.local.db.DrawEntity> -> entities.map { it.toDraw() } }
            .onEach { historyCacheRef.set(it) }

    override fun observeLastDraw(): Flow<Draw?> =
        drawDao.getLastDraw()
            .onStart { runBlocking { ensureInitialized() } }
            .map { entity: com.cebolao.lotofacil.data.local.db.DrawEntity? -> entity?.toDraw() }

    override suspend fun getHistory(): List<Draw> {
        historyCacheRef.get()?.let { cached ->
            if (cached.isNotEmpty()) return cached
        }

        ensureInitialized()
        val draws = drawDao.getAllDrawsSnapshot().map { it.toDraw() }

        if (draws.isNotEmpty()) {
            val toCache = draws.take(100)
            drawCache.putAll(toCache)
            historyCacheRef.set(draws)
        }

        return draws
    }

    override suspend fun getLastDraw(): Draw? {
        ensureInitialized()
        getFreshApiResult()?.toDraw()?.let { fromApi ->
            drawCache.put(fromApi.contestNumber, fromApi)
            return fromApi
        }

        val lastDraw = drawDao.getLastDrawSnapshot()?.toDraw()
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
        ensureInitialized()
        val lastDraw = drawDao.getLastDrawSnapshot()?.toDraw() ?: return null

        val cachedDetails = drawDetailsDao.getDrawDetails(lastDraw.contestNumber)
        if (cachedDetails != null) {
            return cachedDetails.toDrawDetails(lastDraw)
        }

        return getFreshApiResult()?.let { result ->
            val entity = result.toDrawDetailsEntity()
            drawDetailsDao.insertDetails(entity)
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

    // Database initialization logic
    private val initMutex = Mutex()
    private var isInitialized = false

    companion object {
        private const val ASSET_FILENAME = "RESULTADOS_LOTOFACIL.csv"
    }

    private suspend fun ensureInitialized() {
        if (isInitialized) return
        initMutex.withLock {
            if (isInitialized) return
            withContext(ioDispatcher) {
                if (drawDao.count() == 0) {
                    populateFromAssets()
                }
            }
            isInitialized = true
        }
    }

    private suspend fun populateFromAssets() = withContext(ioDispatcher) {
        try {
            logger.info(TAG, "Populating database from assets...")

            var inserted = 0
            val batchSize = 500

            appDatabase.withTransaction {
                context.assets.open(ASSET_FILENAME).bufferedReader().useLines { lines ->
                    val buffer = ArrayList<com.cebolao.lotofacil.data.local.db.DrawEntity>(batchSize)

                    lines
                        .drop(1) // Drop header
                        .filter { it.isNotBlank() }
                        .forEach { line ->
                            val draw = HistoryParser.parseLine(line) ?: return@forEach
                            buffer.add(draw.toEntity())

                            if (buffer.size >= batchSize) {
                                drawDao.insertAll(buffer)
                                inserted += buffer.size
                                buffer.clear()
                            }
                        }

                    if (buffer.isNotEmpty()) {
                        drawDao.insertAll(buffer)
                        inserted += buffer.size
                    }
                }
            }

            logger.info(TAG, "Populated $inserted draws from assets.")
        } catch (e: IOException) {
            logger.error(TAG, "Error populating from assets", e)
        } catch (e: Exception) {
            logger.error(TAG, "Database error populating from assets", e)
        }
    }
}
