package com.cebolao.lotofacil.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.cebolao.lotofacil.data.local.db.AppDatabase
import com.cebolao.lotofacil.data.local.db.DrawDao
import com.cebolao.lotofacil.data.mapper.toEntity
import com.cebolao.lotofacil.data.util.HistoryParser
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.DatabaseLoadingState
import com.cebolao.lotofacil.domain.model.LoadingPhase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for loading lottery data from assets into the database.
 * Provides progress tracking and error handling throughout the loading process.
 */
@Singleton
class DatabaseLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val drawDao: DrawDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private companion object {
        private const val TAG = "DatabaseLoader"
        private const val ASSET_FILENAME = "RESULTADOS_LOTOFACIL.csv"
        private const val BATCH_SIZE = 500
    }

    private val _loadingState = MutableStateFlow<DatabaseLoadingState>(DatabaseLoadingState.Idle)
    val loadingState: StateFlow<DatabaseLoadingState> = _loadingState.asStateFlow()

    private var isLoading = false

    /**
     * Checks if the database needs to be loaded from assets.
     * @return true if database is empty and needs loading
     */
    suspend fun needsLoading(): Boolean = withContext(ioDispatcher) {
        drawDao.count() == 0
    }

    /**
     * Loads data from assets into the database with progress tracking.
     * This is the primary entry point for automated database initialization.
     * @return Result indicating success or failure with error details
     */
    suspend fun loadFromAssets(): Result<Int> = withContext(ioDispatcher) {
        if (isLoading) {
            Log.w(TAG, "Load already in progress, skipping")
            return@withContext Result.failure(IllegalStateException("Load already in progress"))
        }

        isLoading = true
        try {
            val result = performAssetLoading()
            isLoading = false
            result
        } catch (e: Exception) {
            isLoading = false
            _loadingState.value = DatabaseLoadingState.Failed(
                error = "Failed to load database: ${e.message}",
                exception = e
            )
            Result.failure(e)
        }
    }

    private suspend fun performAssetLoading(): Result<Int> = runCatching {
        Log.i(TAG, "Starting database loading from assets")

        _loadingState.value = DatabaseLoadingState.Loading(
            phase = LoadingPhase.CHECKING,
            progress = 0f,
            loadedCount = 0,
            totalCount = 0
        )

        val totalLines = countAssetLines()
        if (totalLines <= 1) {
            throw IOException("Asset file is empty or unreadable")
        }
        val totalDataLines = totalLines - 1
        Log.d(TAG, "Total lines to process: $totalLines")

        var inserted = 0
        var parsed = 0
        var failed = 0

        appDatabase.withTransaction {
            context.assets.open(ASSET_FILENAME).bufferedReader().useLines { lines ->
                val iterator = lines.iterator()
                val headerSkipped = skipHeader(iterator)

                if (headerSkipped) {
                    _loadingState.value = DatabaseLoadingState.Loading(
                        phase = LoadingPhase.READING_ASSETS,
                        progress = 0.05f,
                        loadedCount = 0,
                        totalCount = totalDataLines
                    )
                }

                val buffer = ArrayList<com.cebolao.lotofacil.data.local.db.DrawEntity>(BATCH_SIZE)
                var lineNumber = 1

                for (line in iterator) {
                    lineNumber++
                    parsed++

                    _loadingState.value = DatabaseLoadingState.Loading(
                        phase = LoadingPhase.PARSING_DATA,
                        progress = 0.1f + (parsed.toFloat() / totalDataLines) * 0.3f,
                        loadedCount = inserted,
                        totalCount = totalDataLines
                    )

                    val draw = HistoryParser.parseLine(line)
                    if (draw != null) {
                        buffer.add(draw.toEntity())

                        if (buffer.size >= BATCH_SIZE) {
                            _loadingState.value = DatabaseLoadingState.Loading(
                                phase = LoadingPhase.SAVING_TO_DATABASE,
                                progress = 0.4f + (parsed.toFloat() / totalDataLines) * 0.5f,
                                loadedCount = inserted,
                                totalCount = totalDataLines
                            )

                            drawDao.insertAll(buffer)
                            inserted += buffer.size
                            buffer.clear()

                            Log.d(TAG, "Batch inserted: $inserted draws processed")
                        }
                    } else {
                        failed++
                    }
                }

                if (buffer.isNotEmpty()) {
                    drawDao.insertAll(buffer)
                    inserted += buffer.size
                }
            }
        }

        _loadingState.value = DatabaseLoadingState.Loading(
            phase = LoadingPhase.FINALIZING,
            progress = 0.95f,
            loadedCount = inserted,
            totalCount = inserted
        )

        Log.i(TAG, "Database loading completed: $inserted draws inserted, $failed failed")
        _loadingState.value = DatabaseLoadingState.Completed(loadedCount = inserted)

        inserted
    }

    private fun countAssetLines(): Int {
        return try {
            context.assets.open(ASSET_FILENAME).bufferedReader().useLines { lines ->
                lines.count()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error counting asset lines", e)
            0
        }
    }

    private fun skipHeader(iterator: Iterator<String>): Boolean {
        return try {
            if (iterator.hasNext()) {
                iterator.next()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not skip header", e)
            false
        }
    }

    /**
     * Resets the loading state to idle.
     * Useful when retrying after a failure.
     */
    fun resetState() {
        isLoading = false
        _loadingState.value = DatabaseLoadingState.Idle
    }

    /**
     * Gets the current count of records in the database.
     */
    suspend fun getRecordCount(): Int = withContext(ioDispatcher) {
        drawDao.count()
    }
}
