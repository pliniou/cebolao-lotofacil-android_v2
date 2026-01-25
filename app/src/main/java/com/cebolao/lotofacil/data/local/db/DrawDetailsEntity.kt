package com.cebolao.lotofacil.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cebolao.lotofacil.domain.model.PrizeRate
import com.cebolao.lotofacil.domain.model.WinnerLocation

@Entity(
    tableName = "draw_details",
    indices = [
        Index(value = ["contestNumber"], unique = true)
    ]
)
data class DrawDetailsEntity(
    @PrimaryKey val contestNumber: Int,
    val nextEstimatedPrize: Double,
    val nextContestDate: String?,
    val nextContestNumber: Int?,
    val accumulatedValue05: Double,
    val accumulatedValueSpecial: Double,
    val location: String?,
    val winnersByState: List<WinnerLocation>,
    val prizeRates: List<PrizeRate>
)
