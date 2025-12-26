package com.cebolao.lotofacil.data.datasource

import android.content.Context
import android.util.Log
import com.cebolao.lotofacil.data.HistoryParser
import com.cebolao.lotofacil.data.local.db.DrawDao
import com.cebolao.lotofacil.data.local.db.DrawDetailsDao
import com.cebolao.lotofacil.data.local.db.DrawDetailsEntity
import com.cebolao.lotofacil.data.mapper.toDraw
import com.cebolao.lotofacil.data.mapper.toEntity
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.Draw
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryLocalDS"
private const val ASSET_FILENAME = "RESULTADOS_LOTOFACIL.csv"

interface HistoryLocalDataSource {
    suspend fun getLocalHistory(): List<Draw>
    suspend fun getLastDraw(): Draw?
    fun observeLocalHistory(): Flow<List<Draw>>
    fun observeLastDraw(): Flow<Draw?>
    suspend fun saveNewContests(newDraws: List<Draw>)
    suspend fun getDrawDetails(contestNumber: Int): DrawDetailsEntity?
    suspend fun saveDrawDetails(details: DrawDetailsEntity)
}

@Singleton
class HistoryLocalDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val drawDao: DrawDao,
    private val drawDetailsDao: DrawDetailsDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryLocalDataSource {

    private val initMutex = Mutex()

    override fun observeLocalHistory(): Flow<List<Draw>> = drawDao.getAllDraws().map { entities ->
         entities.map { it.toDraw() }
    }

    override fun observeLastDraw(): Flow<Draw?> = drawDao.getLastDraw().map { entity ->
        entity?.toDraw()
    }

    override suspend fun getLocalHistory(): List<Draw> = withContext(ioDispatcher) {
        ensureInitialized()
        drawDao.getAllDrawsSnapshot().map { it.toDraw() }
    }

    override suspend fun getLastDraw(): Draw? = withContext(ioDispatcher) {
        if (drawDao.count() == 0) {
            ensureInitialized()
        }
        drawDao.getLastDrawSnapshot()?.toDraw()
    }

    override suspend fun getDrawDetails(contestNumber: Int): DrawDetailsEntity? = withContext(ioDispatcher) {
        drawDetailsDao.getDrawDetails(contestNumber)
    }

    override suspend fun saveDrawDetails(details: DrawDetailsEntity) = withContext(ioDispatcher) {
        drawDetailsDao.insertDetails(details)
    }

    override suspend fun saveNewContests(newDraws: List<Draw>) {
        if (newDraws.isEmpty()) return

        withContext(ioDispatcher) {
            val entities = newDraws.map { it.toEntity() }
            drawDao.insertAll(entities)
            Log.d(TAG, "Persisted ${newDraws.size} new contests to Room.")
        }
    }

    private suspend fun ensureInitialized() {
        initMutex.withLock {
            if (drawDao.count() == 0) {
                populateFromAssets()
            }
        }
    }

    private suspend fun populateFromAssets() {
        try {
            Log.i(TAG, "Populating database from assets...")
            val draws = context.assets.open(ASSET_FILENAME).use { inputStream ->
                inputStream.bufferedReader().useLines { lines ->
                    lines
                        .drop(1) // Drop Header
                        .filter { it.isNotBlank() }
                        .mapNotNull { HistoryParser.parseLine(it) }
                        .toList()
                }
            }
            
            val entities = draws.map { it.toEntity() }
            
            entities.chunked(500).forEach { chunk ->
                drawDao.insertAll(chunk)
            }
            Log.i(TAG, "Populated ${entities.size} draws from assets.")
        } catch (e: Exception) {
            Log.e(TAG, "Error populating from assets", e)
        }
    }
}