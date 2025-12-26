package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.data.mapper.toDraw
import com.cebolao.lotofacil.data.mapper.toDrawDetails
import com.cebolao.lotofacil.data.mapper.toDrawDetailsEntity
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.util.toAppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRepository"

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val remoteDataSource: HistoryRemoteDataSource,
    @param:ApplicationScope private val scope: CoroutineScope
) : HistoryRepository {

    @Volatile
    private var latestApiResult: LotofacilApiResult? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    override fun observeHistory(): Flow<List<Draw>> =
        localDataSource.observeLocalHistory()

    override fun observeLastDraw(): Flow<Draw?> = 
        localDataSource.observeLastDraw()

    override suspend fun getHistory(): List<Draw> {
        return localDataSource.getLocalHistory()
    }

    override suspend fun getLastDraw(): Draw? {
        // Try from API Cache first for freshness
        val fromApi = latestApiResult?.toDraw()
        if (fromApi != null) return fromApi
        // Fallback to local DB - Optimized now
        return localDataSource.getLastDraw()
    }

    override suspend fun getLastDrawDetails(): DrawDetails? {
        val lastDraw = localDataSource.getLastDraw() ?: return null
        
        // 1. Try local DB
        val cachedDetails = localDataSource.getDrawDetails(lastDraw.contestNumber)
        if (cachedDetails != null) {
             return cachedDetails.toDrawDetails(lastDraw)
        }

        // 2. Try API
        return latestApiResult?.let { 
             val entity = it.toDrawDetailsEntity()
             localDataSource.saveDrawDetails(entity)
             entity.toDrawDetails(lastDraw) 
        } ?: runCatching {
            remoteDataSource.getLatestDraw()?.also { apiResult -> 
                latestApiResult = apiResult
                val entity = apiResult.toDrawDetailsEntity()
                localDataSource.saveDrawDetails(entity)
            }?.toDrawDetails() // Basic conversion without entity if needed, or use entity flow
        }.getOrNull()
    }

    override fun syncHistory(): Job = scope.launch {
        if (_syncStatus.value is SyncStatus.Syncing) return@launch

        try {
            _syncStatus.value = SyncStatus.Syncing
            performSync()
            _syncStatus.value = SyncStatus.Success
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sync failed", e)
            _syncStatus.value = SyncStatus.Failed(e.toAppError())
        }
    }

    private suspend fun performSync() {
        // Optimization: getLastDraw() is much faster than getLocalHistory()
        val currentMax = localDataSource.getLastDraw()?.contestNumber ?: 0
        val remoteResult = remoteDataSource.getLatestDraw()
            ?: throw IllegalArgumentException("Unable to fetch latest draw from API")

        latestApiResult = remoteResult
        val remoteMax = remoteResult.numero

        if (remoteMax > currentMax) {
            val totalRange = (currentMax + 1)..remoteMax
            val chunks = totalRange.chunked(20)

            for (chunk in chunks) {
                if (chunk.isEmpty()) continue
                val chunkRange = chunk.first()..chunk.last()

                val newDraws = remoteDataSource.getDrawsInRange(chunkRange)
                if (newDraws.isNotEmpty()) {
                    localDataSource.saveNewContests(newDraws)
                }
            }
        }
    }
}