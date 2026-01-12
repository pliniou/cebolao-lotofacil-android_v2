package com.cebolao.lotofacil.ui.model

import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.model.PrizeRate
import com.cebolao.lotofacil.domain.model.WinnerLocation

@Immutable
data class UiDrawDetails(
    val draw: UiDraw,
    val nextEstimatedPrize: Double,
    val nextContestDate: String?,
    val nextContestNumber: Int?,
    val accumulatedValue05: Double,
    val accumulatedValueSpecial: Double,
    val location: String?,
    val winnersByState: List<WinnerLocation>,
    val prizeRates: List<PrizeRate>
)

fun DrawDetails.toUiModel(): UiDrawDetails = UiDrawDetails(
    draw = draw.toUiModel(),
    nextEstimatedPrize = nextEstimatedPrize,
    nextContestDate = nextContestDate,
    nextContestNumber = nextContestNumber,
    accumulatedValue05 = accumulatedValue05,
    accumulatedValueSpecial = accumulatedValueSpecial,
    location = location,
    winnersByState = winnersByState,
    prizeRates = prizeRates
)
