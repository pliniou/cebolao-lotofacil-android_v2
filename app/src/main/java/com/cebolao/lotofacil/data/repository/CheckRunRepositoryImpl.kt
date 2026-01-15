package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.local.db.CheckRunDao
import com.cebolao.lotofacil.data.local.db.CheckRunEntity
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.DrawWindow
import com.cebolao.lotofacil.domain.model.FinancialProjection
import com.cebolao.lotofacil.domain.repository.CheckRunRepository
import com.cebolao.lotofacil.domain.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckRunRepositoryImpl @Inject constructor(
    private val checkRunDao: CheckRunDao,
    private val json: Json,
    private val logger: Logger
) : CheckRunRepository {

    override suspend fun saveCheckRun(report: CheckReport, lotteryId: String): Long {
        val entity = report.toEntity(lotteryId, json)
        return checkRunDao.insert(entity)
    }

    override fun getAllCheckRuns(): Flow<List<CheckReport>> {
        return checkRunDao.getAllCheckRuns().map { entities ->
            entities.mapNotNull { it.toCheckReport(json) }
        }
    }

    override suspend fun getCheckRunByHash(hash: String): CheckReport? {
        return checkRunDao.getCheckRunByHash(hash)?.toCheckReport(json)
    }

    override suspend fun getRecentCheckRuns(limit: Int): List<CheckReport> {
        return checkRunDao.getRecentCheckRuns(limit).mapNotNull { it.toCheckReport(json) }
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
            logger.warning("CheckRunRepository", "Failed to decode CheckRunEntity id=$id", null)
            null
        }
    }
}
