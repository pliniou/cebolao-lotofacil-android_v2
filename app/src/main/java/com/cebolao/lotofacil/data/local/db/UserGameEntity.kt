package com.cebolao.lotofacil.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidade Room para persistir jogos do usuário
 */
@Entity(tableName = "user_games")
data class UserGameEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val lotteryId: String = "lotofacil",
    val numbersMask: Long,
    val createdAt: Long,
    val pinned: Boolean = false,
    val tags: String = "[]", // JSON array de strings
    val source: String = "manual", // "manual", "generated", "imported", etc.
    val seed: Long? = null // Seed usado na geração (se aplicável)
)

