package com.cebolao.lotofacil.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para persistir conferências de jogos
 */
@Entity(tableName = "check_runs")
data class CheckRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketMask: Long,
    val lotteryId: String,
    val drawRange: String, // JSON com DrawWindow
    val createdAt: Long,
    val metricsJSON: String, // JSON com FinancialProjection
    val sourceHash: String
)

