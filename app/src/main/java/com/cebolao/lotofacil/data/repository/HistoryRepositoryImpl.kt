package com.cebolao.lotofacil.data.repository

import android.content.Context
import android.util.Log
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
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
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
    @param:ApplicationScope private val scope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryRepository {

    private val drawCache = DrawLruCache(maxSize = 100)
    private val latestApiResultRef = AtomicReference<LatestApiResult?>()
    private val historyCacheRef = AtomicReference<List<Draw>?>(null)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val syncMutex = Mutex()
    private var syncDeferred: Deferred<AppResult<Unit>>? = null
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

    override suspend fun getHistory(): AppResult<List<Draw>> {
        historyCacheRef.get()?.let { cached ->
            if (cached.isNotEmpty()) return AppResult.Success(cached)
        }

        return runCatching {
            ensureInitialized()
            val draws = drawDao.getAllDrawsSnapshot().map { it.toDraw() }

            if (draws.isNotEmpty()) {
                val toCache = draws.take(100)
                drawCache.putAll(toCache)
                historyCacheRef.set(draws)
            }

            AppResult.Success(draws)
        }.getOrElse { e ->
            Log.e(TAG, "Failed to load history", e)
            AppResult.Failure(e.toAppError())
        }
    }

    override suspend fun getLastDraw(): AppResult<Draw?> = runCatching {
        ensureInitialized()
        getFreshApiResult()?.toDraw()?.let { fromApi ->
            drawCache.put(fromApi.contestNumber, fromApi)
            return@runCatching AppResult.Success(fromApi)
        }

        val lastDraw = drawDao.getLastDrawSnapshot()?.toDraw()
        if (lastDraw != null) {
            drawCache.put(lastDraw.contestNumber, lastDraw)
            return@runCatching AppResult.Success(lastDraw)
        }

        val lastFromCache = drawCache.getAll().maxByOrNull { it.contestNumber }
        if (lastFromCache != null) {
            Log.d(TAG, "Returning last draw from cache: ${lastFromCache.contestNumber}")
            return@runCatching AppResult.Success(lastFromCache)
        }

        AppResult.Success(null)
    }.getOrElse { e ->
        Log.e(TAG, "Failed to load last draw", e)
        AppResult.Failure(e.toAppError())
    }

    override suspend fun getLastDrawDetails(): AppResult<DrawDetails?> = runCatching {
        ensureInitialized()
        val lastDraw = drawDao.getLastDrawSnapshot()?.toDraw() ?: return@runCatching AppResult.Success(null)

        val cachedDetails = drawDetailsDao.getDrawDetails(lastDraw.contestNumber)
        if (cachedDetails != null) {
            return@runCatching AppResult.Success(cachedDetails.toDrawDetails(lastDraw))
        }

        val details = getFreshApiResult()?.let { result ->
            val entity = result.toDrawDetailsEntity()
            drawDetailsDao.insertDetails(entity)
            entity.toDrawDetails(lastDraw)
        }
        AppResult.Success(details)
    }.getOrElse { e ->
        Log.e(TAG, "Failed to load last draw details", e)
        AppResult.Failure(e.toAppError())
    }

    override fun syncHistory(): Job = synchronized(this) {
        runningSyncJob?.takeIf { it.isActive }?.let { return it }

        val job = scope.launch(ioDispatcher) {
            syncHistoryIfNeeded()
        }
        runningSyncJob = job
        return job
    }

    override suspend fun syncHistoryIfNeeded(): AppResult<Unit> {
        val deferred = syncMutex.withLock {
            val inFlight = syncDeferred?.takeIf { it.isActive }
            if (inFlight != null) {
                Log.d(TAG, "Sync already in flight, joining existing work")
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

    private suspend fun runSyncIfNeeded(): AppResult<Unit> {
        val shouldSync = try {
            syncManager.shouldSync()
        } catch (e: IOException) {
            Log.e(TAG, "Network error checking sync status", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            return AppResult.Failure(e.toAppError())
        } catch (e: SyncException) {
            Log.e(TAG, "Sync service error checking sync status", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            return AppResult.Failure(e.toAppError())
        }

        if (!shouldSync) {
            Log.i(TAG, "Sync not needed, skipping")
            _syncStatus.value = SyncStatus.Success
            return AppResult.Success(Unit)
        }

        return try {
            _syncStatus.value = SyncStatus.Syncing
            Log.i(TAG, "Starting history sync")

            val latestResult = syncManager.performIncrementalSync()
            cacheLatestApiResult(latestResult)

            drawCache.clear()
            historyCacheRef.set(null)
            Log.d(TAG, "Cache cleared after sync")

            _syncStatus.value = SyncStatus.Success
            Log.i(TAG, "Sync completed successfully")
            AppResult.Success(Unit)
        } catch (e: IOException) {
            cacheLatestApiResult(null)
            Log.e(TAG, "Network sync failed", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            AppResult.Failure(e.toAppError())
        } catch (e: SyncException) {
            cacheLatestApiResult(null)
            Log.e(TAG, "Sync service error", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
            AppResult.Failure(e.toAppError())
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
            Log.i(TAG, "Populating database from assets...")

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

            Log.i(TAG, "Populated $inserted draws from assets.")
        } catch (e: IOException) {
            Log.e(TAG, "Error populating from assets", e)
        } catch (e: Exception) {
            Log.e(TAG, "Database error populating from assets", e)
        }
    }
}
