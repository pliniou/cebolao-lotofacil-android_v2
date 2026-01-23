package com.cebolao.lotofacil.ui.model

import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.domain.model.NumberFrequency
import com.cebolao.lotofacil.domain.model.StatisticsReport
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class UiStatisticsReport(
    val mostFrequentNumbers: List<NumberFrequency>,
    val mostOverdueNumbers: List<NumberFrequency>,
    val evenDistribution: Map<Int, Int>,
    val primeDistribution: Map<Int, Int>,
    val frameDistribution: Map<Int, Int>,
    val sumDistribution: Map<Int, Int>,
    val fibonacciDistribution: Map<Int, Int>,
    val multiplesOf3Distribution: Map<Int, Int>,
    val centerDistribution: Map<Int, Int>,
    val sequencesDistribution: Map<Int, Int>,
    val averageSum: Float,
    val totalDrawsAnalyzed: Int,
    val analysisDate: String
)

fun StatisticsReport.toUiModel(): UiStatisticsReport = UiStatisticsReport(
    mostFrequentNumbers = mostFrequentNumbers,
    mostOverdueNumbers = mostOverdueNumbers,
    evenDistribution = evenDistribution,
    primeDistribution = primeDistribution,
    frameDistribution = frameDistribution,
    sumDistribution = sumDistribution,
    fibonacciDistribution = fibonacciDistribution,
    multiplesOf3Distribution = multiplesOf3Distribution,
    centerDistribution = centerDistribution,
    sequencesDistribution = sequencesDistribution,
    averageSum = averageSum,
    totalDrawsAnalyzed = totalDrawsAnalyzed,
    analysisDate = Instant.ofEpochMilli(analysisDate)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()))
)
