package com.cebolao.lotofacil.ui.model

import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.domain.model.CheckReport
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class UiGameAnalysisResult(
    val game: UiLotofacilGame,
    val simpleStats: ImmutableList<Pair<String, String>>,
    val checkReport: CheckReport // CheckReport can also be mapped to UiCheckReport if needed
)
