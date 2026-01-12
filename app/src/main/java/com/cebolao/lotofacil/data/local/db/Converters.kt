package com.cebolao.lotofacil.data.local.db

import androidx.room.TypeConverter
import com.cebolao.lotofacil.domain.model.PrizeRate
import com.cebolao.lotofacil.domain.model.WinnerLocation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromPrizeRateList(value: List<PrizeRate>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toPrizeRateList(value: String): List<PrizeRate> {
        return try {
            Json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromWinnerLocationList(value: List<WinnerLocation>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toWinnerLocationList(value: String): List<WinnerLocation> {
        return try {
            Json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
