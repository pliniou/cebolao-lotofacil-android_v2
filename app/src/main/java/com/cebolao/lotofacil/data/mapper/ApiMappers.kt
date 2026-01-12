package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.domain.model.Draw
import java.text.SimpleDateFormat
import java.util.Locale

fun LotofacilApiResult.toDraw(): Draw {
    val numbers = listaDezenas.mapNotNull { it.toIntOrNull() }.toSet()
    val dateMillis = dataApuracao.parseDateToMillis()
    
    return Draw.fromNumbers(contestNumber = numero, numbers = numbers, date = dateMillis)
}

private fun String?.parseDateToMillis(): Long? {
    if (this == null) return null
    return try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        format.parse(this)?.time
    } catch (_: Exception) {
        null
    }
}
