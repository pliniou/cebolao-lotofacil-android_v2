package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.domain.model.CheckReport
import kotlinx.coroutines.flow.Flow

/**
 * Repository para persistir e recuperar conferências de jogos
 */
interface CheckRunRepository {
    /**
     * Salva uma conferência no banco de dados
     */
    suspend fun saveCheckRun(report: CheckReport, lotteryId: String = "lotofacil"): Long

    /**
     * Busca todas as conferências
     */
    fun getAllCheckRuns(): Flow<List<CheckReport>>

    /**
     * Busca conferências por hash do ticket
     */
    suspend fun getCheckRunByHash(hash: String): CheckReport?

    /**
     * Busca conferências recentes
     */
    suspend fun getRecentCheckRuns(limit: Int): List<CheckReport>
}
