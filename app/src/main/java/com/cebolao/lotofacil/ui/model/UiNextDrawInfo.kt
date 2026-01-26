package com.cebolao.lotofacil.ui.model

import java.time.LocalDate

data class UiNextDrawInfo(
    val contestNumber: Int,
    val formattedDate: String,
    val formattedPrize: String,
    val formattedPrizeFinalFive: String,
    val drawDate: LocalDate? = null
)
