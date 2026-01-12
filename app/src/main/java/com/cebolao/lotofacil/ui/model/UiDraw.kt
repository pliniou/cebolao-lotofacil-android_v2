package com.cebolao.lotofacil.ui.model

import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.domain.model.BitmaskProvider
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.GameStatisticsProvider

@Immutable
data class UiDraw(
    val contestNumber: Int,
    override val numbers: Set<Int>,
    val date: Long? = null,
    override val mask: Long = 0L // Default or required? Draw has it.
) : GameStatisticsProvider, BitmaskProvider

fun Draw.toUiModel(): UiDraw = UiDraw(
    contestNumber = contestNumber,
    numbers = numbers,
    date = date,
    mask = mask
)
