package com.cebolao.lotofacil.data.util

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.Draw
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DELIMITER_SEMICOLON = ';'
private const val MIN_COLUMNS = 29
private const val CONTEST_NUMBER_INDEX = 0
private const val DRAW_DATE_INDEX = 1
private const val FIRST_BALL_INDEX = 2

/**
 * Parses lines from RESULTADOS_LOTOFACIL.csv
 * Format:
 * Concurso;Data Sorteio;Bola1..Bola15;...;Acumulado 15 acertos;...
 * 
 * Uses java.time for efficient and safe date parsing.
 */
object HistoryParser {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
    private val zoneId = ZoneId.systemDefault()

    fun parseLine(line: String): Draw? {
        val raw = line.trim()
        if (raw.isEmpty()) return null

        val parts = raw.split(DELIMITER_SEMICOLON)
        if (parts.size < MIN_COLUMNS) return null

        val contestNumber = parts.getOrNull(CONTEST_NUMBER_INDEX)?.trim()?.toIntOrNull() ?: return null
        val dateStr = parts.getOrNull(DRAW_DATE_INDEX)?.trim().orEmpty()
        val dateLong = parseDateMillis(dateStr)
        val numbers = HashSet<Int>(GameConstants.GAME_SIZE * 2)
        val lastBallIndexInclusive = FIRST_BALL_INDEX + GameConstants.GAME_SIZE - 1

        for (i in FIRST_BALL_INDEX..lastBallIndexInclusive) {
            val num = parts.getOrNull(i)?.trim()?.toIntOrNull() ?: continue
            numbers.add(num)
        }

        if (!isValidDraw(numbers)) return null

        return Draw.fromNumbers(contestNumber = contestNumber, numbers = numbers, date = dateLong)
    }

    private fun parseDateMillis(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return runCatching {
            val localDate = LocalDate.parse(dateStr, dateFormatter)
            localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }.getOrNull()
    }

    private fun isValidDraw(numbers: Set<Int>): Boolean {
        return numbers.size == GameConstants.GAME_SIZE &&
                numbers.all { it in GameConstants.NUMBER_RANGE }
    }
}
