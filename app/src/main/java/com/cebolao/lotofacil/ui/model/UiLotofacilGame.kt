package com.cebolao.lotofacil.ui.model

import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.domain.model.LotofacilGame

@Immutable
data class UiLotofacilGame(
    val numbers: Set<Int>,
    val isPinned: Boolean,
    val creationTimestamp: Long,
    val mask: Long // Mantendo mask para identificação se necessário, ou usar hash
)

fun LotofacilGame.toUiModel(): UiLotofacilGame = UiLotofacilGame(
    numbers = numbers,
    isPinned = isPinned,
    creationTimestamp = creationTimestamp,
    mask = mask
)

// Converter de volta se necessário (geralmente ações da UI retornam ID/Objeto para ViewModel que sabe lidar com Domain)
fun UiLotofacilGame.toDomain(): LotofacilGame = LotofacilGame.fromMask(
    mask = mask,
    isPinned = isPinned,
    timestamp = creationTimestamp
)
