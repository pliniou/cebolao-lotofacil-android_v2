package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.domain.model.Draw
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LotofacilApiResult.toDraw(): Draw {
    val mask = numbersToMask(listaDezenas)
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

private fun numbersToMask(numbers: List<String>): Long {
    var mask = 0L
    for (s in numbers) {
        val n = s.toIntOrNull() ?: continue
        val idx = n - 1
        if (idx in 0..63) mask = mask or (1L shl idx)
    }
    return mask
}
