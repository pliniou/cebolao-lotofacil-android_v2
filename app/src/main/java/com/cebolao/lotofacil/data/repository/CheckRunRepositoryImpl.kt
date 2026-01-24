package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.local.db.CheckRunDao
import com.cebolao.lotofacil.data.local.db.CheckRunEntity
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.DrawWindow
import com.cebolao.lotofacil.domain.model.FinancialProjection
import com.cebolao.lotofacil.domain.repository.CheckRunRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CheckRunRepository"

@Singleton
class CheckRunRepositoryImpl @Inject constructor(
    private val checkRunDao: CheckRunDao,
    private val json: Json,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CheckRunRepository {

    override suspend fun saveCheckRun(report: CheckReport, lotteryId: String): Long {
        val entity = report.toEntity(lotteryId, json)
        return withContext(ioDispatcher) { checkRunDao.insert(entity) }
    }

    override fun getAllCheckRuns(): Flow<List<CheckReport>> {
        return checkRunDao.getAllCheckRuns().map { entities ->
            entities.mapNotNull { it.toCheckReport(json) }
        }.flowOn(ioDispatcher)
    }

    override suspend fun getCheckRunByHash(hash: String): CheckReport? {
        return withContext(ioDispatcher) {
            checkRunDao.getCheckRunByHash(hash)?.toCheckReport(json)
        }
    }

    override suspend fun getRecentCheckRuns(limit: Int): List<CheckReport> {
        return withContext(ioDispatcher) {
            checkRunDao.getRecentCheckRuns(limit).mapNotNull { it.toCheckReport(json) }
        }
    }

    private fun CheckReport.toEntity(lotteryId: String, json: Json): CheckRunEntity {
        return CheckRunEntity(
            id = 0,
            ticketMask = ticket.mask,
            lotteryId = lotteryId,
            drawRange = json.encodeToString(DrawWindow.serializer(), drawWindow),
            createdAt = timestamp,
            metricsJSON = json.encodeToString(FinancialProjection.serializer(), financialMetrics),
            hitsJSON = json.encodeToString(ListSerializer(com.cebolao.lotofacil.domain.model.Hit.serializer()), hits),
            sourceHash = sourceHash
        )
    }

    private fun CheckRunEntity.toCheckReport(json: Json): CheckReport? {
        return try {
            val drawWindow = json.decodeFromString(DrawWindow.serializer(), drawRange)
            val financialMetrics = json.decodeFromString(FinancialProjection.serializer(), metricsJSON)
            val ticket = com.cebolao.lotofacil.domain.model.LotofacilGame.fromMask(ticketMask)
            val hits = json.decodeFromString(ListSerializer(com.cebolao.lotofacil.domain.model.Hit.serializer()), hitsJSON)
            
            CheckReport(
                ticket = ticket,
                drawWindow = drawWindow,
                hits = hits,
                financialMetrics = financialMetrics,
                timestamp = createdAt,
                sourceHash = sourceHash
            )
        } catch (_: Exception) {
            Log.w(TAG, "Failed to decode CheckRunEntity id=$id")
            null
        }
    }
}
