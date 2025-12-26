package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.local.db.DrawEntity
import com.cebolao.lotofacil.domain.model.Draw

fun DrawEntity.toDraw(): Draw {
    return Draw.fromNumbers(
        contestNumber = contestNumber,
        numbers = numbers.split(",").mapNotNull { it.toIntOrNull() }.toSet(),
        date = date
    )
}

fun Draw.toEntity(): DrawEntity {
    return DrawEntity(
        contestNumber = contestNumber,
        numbers = numbers.sorted().joinToString(","),
        date = date
    )
}
