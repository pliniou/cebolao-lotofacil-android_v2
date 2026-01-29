package com.cebolao.lotofacil.domain.model

/**
 * UI model for a Lotofácil game
 */
data class UiLotofacilGame(
    val numbers: Set<Int>,
    val isPinned: Boolean,
    val creationTimestamp: Long,
    val mask: Long
)
