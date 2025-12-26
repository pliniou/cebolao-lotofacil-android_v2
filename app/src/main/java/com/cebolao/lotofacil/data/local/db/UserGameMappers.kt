package com.cebolao.lotofacil.data.local.db

import com.cebolao.lotofacil.domain.model.LotofacilGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Converte UserGameEntity para LotofacilGame
 */
fun UserGameEntity.toLotofacilGame(): LotofacilGame {
    return LotofacilGame.fromMask(
        mask = numbersMask,
        isPinned = pinned,
        timestamp = createdAt
    )
}

/**
 * Converte LotofacilGame para UserGameEntity
 */
fun LotofacilGame.toUserGameEntity(
    id: String? = null,
    lotteryId: String = "lotofacil",
    source: String = "manual",
    seed: Long? = null,
    tags: List<String> = emptyList(),
    pinned: Boolean? = null,
    json: Json
): UserGameEntity {
    val tagsJson = json.encodeToString(tags)
    return UserGameEntity(
        id = id ?: java.util.UUID.randomUUID().toString(),
        lotteryId = lotteryId,
        numbersMask = mask,
        createdAt = creationTimestamp,
        pinned = pinned ?: isPinned,
        tags = tagsJson,
        source = source,
        seed = seed
    )
}

/**
 * Parse tags JSON string para List<String>
 */
fun parseTagsJson(tagsJson: String, json: Json): List<String> {
    return try {
        json.decodeFromString<List<String>>(tagsJson)
    } catch (e: Exception) {
        emptyList()
    }
}

