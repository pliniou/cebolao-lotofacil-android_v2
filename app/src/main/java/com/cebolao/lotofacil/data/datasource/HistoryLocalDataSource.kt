package com.cebolao.lotofacil.data.datasource

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.cebolao.lotofacil.data.HistoryParser
import com.cebolao.lotofacil.data.local.db.AppDatabase
import com.cebolao.lotofacil.data.local.db.DrawDao
import com.cebolao.lotofacil.data.local.db.DrawDetailsDao
import com.cebolao.lotofacil.data.local.db.DrawDetailsEntity
import com.cebolao.lotofacil.data.mapper.toDraw
import com.cebolao.lotofacil.data.mapper.toEntity
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
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
    private val appDatabase: AppDatabase,
    private val drawDao: DrawDao,
    private val drawDetailsDao: DrawDetailsDao,
    private val logger: Logger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryLocalDataSource {

    private val initMutex = Mutex()
    private var isInitialized = false

    override fun observeLocalHistory(): Flow<List<Draw>> = drawDao.getAllDraws()
        .onStart { ensureInitialized() }
        .map { entities ->
            entities.map { it.toDraw() }
        }
        .flowOn(ioDispatcher) // Execute mapping on IO

    override fun observeLastDraw(): Flow<Draw?> = drawDao.getLastDraw()
        .onStart { ensureInitialized() }
        .map { entity ->
            entity?.toDraw()
        }
        .flowOn(ioDispatcher) // Execute mapping on IO

    override suspend fun getLocalHistory(): List<Draw> = withContext(ioDispatcher) {
        ensureInitialized()
        drawDao.getAllDrawsSnapshot().map { it.toDraw() }
    }

    override suspend fun getLastDraw(): Draw? = withContext(ioDispatcher) {
        ensureInitialized()
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
            appDatabase.withTransaction {
                drawDao.insertAll(entities)
            }
            logger.debug(TAG, "Persisted ${newDraws.size} new contests to Room.")
        }
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
            
            appDatabase.withTransaction {
                entities.chunked(500).forEach { chunk ->
                    drawDao.insertAll(chunk)
                }
            }
            logger.info(TAG, "Populated ${entities.size} draws from assets.")
        } catch (e: IOException) {
            logger.error(TAG, "Error populating from assets", e)
        } catch (e: SQLiteException) {
            logger.error(TAG, "Database error populating from assets", e)
        }
    }
}
