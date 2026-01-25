package com.cebolao.lotofacil.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room para persistir conferências de jogos
 */
@Entity(
    tableName = "check_runs",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["lotteryId"]),
        Index(value = ["ticketMask"])
    ]
)
data class CheckRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketMask: Long,
    val lotteryId: String,
    val drawRange: String,
    val createdAt: Long,
    val metricsJSON: String,
    val hitsJSON: String,
    val sourceHash: String
)
