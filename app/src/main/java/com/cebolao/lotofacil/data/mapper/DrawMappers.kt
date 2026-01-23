package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.local.db.DrawEntity
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.MaskUtils

fun DrawEntity.toDraw(): Draw {
    val mask = csvToMaskFast(numbers)
    return Draw(contestNumber, mask, date)
}

fun Draw.toEntity(): DrawEntity {
    return DrawEntity(
        contestNumber = contestNumber,
        numbers = maskToCsv(mask),
        date = date
    )
}

private fun csvToMaskFast(csv: String): Long {
    if (csv.isBlank()) return 0L

    var mask = 0L
    var current = 0
    var inNumber = false

    for (c in csv) {
        when {
            c in '0'..'9' -> {
                current = (current * 10) + (c - '0')
                inNumber = true
            }
            c == ',' -> {
                if (inNumber) {
                    val idx = current - 1
                    if (idx in 0..63) mask = mask or (1L shl idx)
                    current = 0
                    inNumber = false
                }
            }
            else -> {
                // ignore whitespace/other separators
            }
        }
    }

    if (inNumber) {
        val idx = current - 1
        if (idx in 0..63) mask = mask or (1L shl idx)
    }

    // Defensive fallback for corrupted rows: keep behavior close to previous mapper.
    if (mask == 0L && csv.any { it.isDigit() }) {
        return MaskUtils.toMask(csv.split(",").mapNotNull { it.toIntOrNull() }.toSet())
    }

    return mask
}

private fun maskToCsv(mask: Long): String {
    if (mask == 0L) return ""

    val sb = StringBuilder(64)
    MaskUtils.forEachNumber(mask) { n ->
        if (sb.isNotEmpty()) sb.append(',')
        sb.append(n)
    }
    return sb.toString()
}
