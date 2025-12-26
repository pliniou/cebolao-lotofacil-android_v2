package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class HomeScreenData(
    val lastDraw: Draw?,
    val initialStats: StatisticsReport,
    val details: DrawDetails?,
    val lastDrawCheckResult: CheckReport? = null
)
