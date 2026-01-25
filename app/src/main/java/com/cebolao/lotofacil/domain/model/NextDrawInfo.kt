package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import java.time.LocalDate

@Stable
@Immutable
data class NextDrawInfo(
    val contestNumber: Int,
    val formattedDate: String,
    val formattedPrize: String,
    val formattedPrizeFinalFive: String,
    val drawDate: LocalDate? = null
)
