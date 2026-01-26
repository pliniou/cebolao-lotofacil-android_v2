package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.MaskUtils
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LotofacilApiResult.toDraw(): Draw {
    return toValidatedDrawOrNull()
        ?: Draw(numero, listaDezenas.toMask(), dataApuracao.parseDateToMillis())
}

/**
 * Validates API payload before creating a domain draw.
 * Ensures correct amount of numbers and range.
 */
fun LotofacilApiResult.toValidatedDrawOrNull(): Draw? {
    val numbers = listaDezenas.toValidatedNumbers() ?: return null
    val mask = MaskUtils.toMask(numbers.toSet())
    val dateMillis = dataApuracao.parseDateToMillis()
    return Draw(numero, mask, dateMillis)
}

private val LOCALE_PT_BR: Locale = Locale.forLanguageTag("pt-BR")
private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PT_BR)

private fun String?.parseDateToMillis(): Long? {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return null

    return runCatching {
        LocalDate.parse(raw, DATE_FORMATTER)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun List<String>.toValidatedNumbers(): List<Int>? {
    val parsed = mapNotNull { it.toIntOrNull() }
    if (parsed.size != GameConstants.GAME_SIZE) return null
    if (parsed.any { it !in GameConstants.NUMBER_RANGE }) return null
    return parsed
}

private fun List<String>.toMask(): Long {
    var mask = 0L
    for (s in this) {
        val n = s.toIntOrNull() ?: continue
        val idx = n - 1
        if (idx in 0..63) mask = mask or (1L shl idx)
    }
    return mask
}
