package com.cebolao.lotofacil.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidade Room para persistir jogos do usuário
 */
@Entity(
    tableName = "user_games",
    indices = [
        Index(value = ["pinned"]),
        Index(value = ["createdAt"]),
        Index(value = ["source"])
    ]
)

data class UserGameEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val lotteryId: String = "lotofacil",
    val numbersMask: Long,
    val createdAt: Long,
    val pinned: Boolean = false,
    val tags: String = "[]",
    val source: String = "manual",
    val seed: Long? = null
)
