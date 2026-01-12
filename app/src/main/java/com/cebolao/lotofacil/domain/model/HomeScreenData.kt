package com.cebolao.lotofacil.domain.model

data class HomeScreenData(
    val lastDraw: Draw?,
    val details: DrawDetails?,
    val lastDrawCheckResult: CheckReport? = null
)
