package com.cebolao.lotofacil.ui.model

import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.domain.model.LotofacilGame

@Immutable
data class UiLotofacilGame(
    val numbers: Set<Int>,
    val isPinned: Boolean,
    val creationTimestamp: Long,
    val mask: Long
)

fun LotofacilGame.toUiModel(): UiLotofacilGame = UiLotofacilGame(
    numbers = numbers,
    isPinned = isPinned,
    creationTimestamp = creationTimestamp,
    mask = mask
)

// Convert back if needed (usually UI actions return ID/Object to ViewModel that knows how to handle Domain)
fun UiLotofacilGame.toDomain(): LotofacilGame = LotofacilGame.fromMask(
    mask = mask,
    isPinned = isPinned,
    timestamp = creationTimestamp
)
