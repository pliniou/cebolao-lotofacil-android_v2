package com.cebolao.lotofacil.ui.components.stats

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.ui.components.common.CustomChip
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.model.UiDraw
import com.cebolao.lotofacil.ui.model.UiStatisticsReport
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun DistributionChartsCard(
    stats: UiStatisticsReport,
    selectedPattern: StatisticPattern,
    onPatternSelected: (StatisticPattern) -> Unit,
    lastDraw: UiDraw? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val chartData = remember(selectedPattern, stats) {
        prepareData(stats, selectedPattern)
    }
    val maxValue = remember(chartData) { chartData.maxOfOrNull { it.second } ?: 0 }

    val (mean, stdDev) = remember(chartData) { calculateWeightedMeanAndStdDev(chartData) }
    val showNormalLine = remember(mean, stdDev, selectedPattern) {
        // Linha normal faz sentido quando temos dados numéricos suficientes e distribuição razoável
        mean != null && stdDev != null && stdDev > 0.0 && chartData.size >= 3
    }

    // Valor do último sorteio para destacar no gráfico
    val highlightValue = remember(lastDraw, selectedPattern) {
        lastDraw?.let { draw ->
            val rawValue = calculatePatternValue(draw, selectedPattern)
            if (selectedPattern == StatisticPattern.SUM) {
                // "buckets" na mesma granularidade do histograma
                ((rawValue / GameConstants.SUM_STEP) * GameConstants.SUM_STEP).toString()
            } else {
                rawValue.toString()
            }
        }
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Solid,
        title = stringResource(R.string.home_distribution_title),
        contentPadding = Dimen.Spacing12,
        headerActions = {
            // Legenda do destaque (último concurso)
            if (highlightValue != null) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = scheme.error)
                }
                Text(
                    text = stringResource(R.string.distribution_last_label, highlightValue),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            // Pattern Selector
            PatternSelector(
                selectedPattern = selectedPattern,
                onPatternSelected = onPatternSelected
            )

            BarChart(
                data = chartData.toImmutableList(),
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.BarChartHeight),
                chartHeight = Dimen.BarChartHeight,
                chartType = ChartType.BAR,
                showNormalLine = showNormalLine,
                mean = mean?.toFloat(),
                stdDev = stdDev?.toFloat(),
                highlightValue = highlightValue
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PatternSelector(
    selectedPattern: StatisticPattern,
    onPatternSelected: (StatisticPattern) -> Unit
) {
    val patterns = remember { StatisticPattern.entries }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
    ) {
        patterns.forEach { pattern ->
            val label = patternLabel(pattern)
            CustomChip(
                selected = pattern == selectedPattern,
                onClick = { onPatternSelected(pattern) },
                label = label
            )
        }
    }
}

@Composable
private fun patternLabel(pattern: StatisticPattern): String = when (pattern) {
    StatisticPattern.SUM -> stringResource(R.string.stat_sum)
    StatisticPattern.EVENS -> stringResource(R.string.stat_evens)
    StatisticPattern.PRIMES -> stringResource(R.string.stat_primes)
    StatisticPattern.FRAME -> stringResource(R.string.stat_frame)
    StatisticPattern.CENTER -> stringResource(R.string.stat_center)
    StatisticPattern.FIBONACCI -> stringResource(R.string.stat_fibonacci)
    StatisticPattern.MULTIPLES_OF_3 -> stringResource(R.string.stat_multiples_of_3)
    StatisticPattern.SEQUENCES -> stringResource(R.string.stat_sequences)
}

private fun calculatePatternValue(draw: UiDraw, pattern: StatisticPattern): Int = when (pattern) {
    StatisticPattern.SUM -> draw.sum
    StatisticPattern.EVENS -> draw.evens
    StatisticPattern.PRIMES -> draw.primes
    StatisticPattern.FRAME -> draw.frame
    StatisticPattern.FIBONACCI -> draw.fibonacci
    StatisticPattern.SEQUENCES -> draw.sequences
    StatisticPattern.MULTIPLES_OF_3 -> draw.multiplesOf3
    StatisticPattern.CENTER -> draw.center
}

private fun calculateWeightedMeanAndStdDev(
    data: List<Pair<String, Int>>
): Pair<Double?, Double?> {
    val numeric = data.mapNotNull { (label, count) ->
        try {
            Integer.parseInt(label) to count
        } catch (_: NumberFormatException) {
            null
        }
    }
    if (numeric.isEmpty()) return null to null

    val total = numeric.sumOf { it.second }
    if (total <= 0) return null to null

    val mean = numeric.sumOf { (x, c) -> x * c.toDouble() } / total
    val variance = numeric.sumOf { (x, c) -> c * (x - mean).pow(2) } / total
    return mean to sqrt(variance)
}

private fun prepareData(
    stats: UiStatisticsReport,
    pattern: StatisticPattern
): List<Pair<String, Int>> {
    val raw: Map<Int, Int> = when (pattern) {
        StatisticPattern.SUM -> stats.sumDistribution
        StatisticPattern.EVENS -> stats.evenDistribution
        StatisticPattern.PRIMES -> stats.primeDistribution
        StatisticPattern.FRAME -> stats.frameDistribution
        StatisticPattern.FIBONACCI -> stats.fibonacciDistribution
        StatisticPattern.SEQUENCES -> stats.sequencesDistribution
        StatisticPattern.MULTIPLES_OF_3 -> stats.multiplesOf3Distribution
        StatisticPattern.CENTER -> stats.centerDistribution
    }
    if (raw.isEmpty()) return emptyList()

    // Para SOMA, respeitar range e step do AppConfig
    if (pattern == StatisticPattern.SUM) {
        val buckets = (GameConstants.SUM_MIN_RANGE..GameConstants.SUM_MAX_RANGE step GameConstants.SUM_STEP)
            .associateWith { 0 }
            .toMutableMap()

        raw.forEach { (k, v) -> if (buckets.containsKey(k)) buckets[k] = v }

        return buckets.entries
            .sortedBy { it.key }
            .map { it.key.toString() to it.value }
    }

    // Demais padrões: preencher “buracos” para suavizar leitura
    val domain = domainFor(pattern)
    val minKey = raw.keys.minOrNull() ?: 0
    val maxKey = raw.keys.maxOrNull() ?: 0
    val rangeToUse = domain ?: (minKey..maxKey)

    val buckets = rangeToUse.associateWith { 0 }.toMutableMap()
    raw.forEach { (k, v) -> if (buckets.containsKey(k)) buckets[k] = v }

    val inRange = buckets.entries.sortedBy { it.key }.map { it.key.toString() to it.value }
    val outOfRange = raw.entries
        .filter { !rangeToUse.contains(it.key) }
        .sortedBy { it.key }
        .map { it.key.toString() to it.value }

    return inRange + outOfRange
}

/** Domínios discretos “naturais” para eixos consistentes. */
private fun domainFor(pattern: StatisticPattern): IntRange? = when (pattern) {
    StatisticPattern.EVENS -> 0..15
    StatisticPattern.PRIMES -> 0..15
    StatisticPattern.FRAME -> 0..15
    StatisticPattern.FIBONACCI -> 0..15
    StatisticPattern.SEQUENCES -> 0..10
    StatisticPattern.MULTIPLES_OF_3 -> 0..15
    StatisticPattern.CENTER -> 0..15
    StatisticPattern.SUM -> null
}

@Preview(showBackground = true)
@Composable
private fun DistributionChartsCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DistributionChartsCard(
                stats = UiStatisticsReport(
                    mostFrequentNumbers = emptyList(),
                    mostOverdueNumbers = emptyList(),
                    evenDistribution = emptyMap(),
                    primeDistribution = emptyMap(),
                    frameDistribution = emptyMap(),
                    sumDistribution = emptyMap(),
                    fibonacciDistribution = emptyMap(),
                    multiplesOf3Distribution = emptyMap(),
                    centerDistribution = emptyMap(),
                    sequencesDistribution = emptyMap(),
                    averageSum = 0f,
                    totalDrawsAnalyzed = 10,
                    analysisDate = "01/01/2026"
                ),
                selectedPattern = StatisticPattern.EVENS,
                onPatternSelected = {}
            )
        }
    }
}
