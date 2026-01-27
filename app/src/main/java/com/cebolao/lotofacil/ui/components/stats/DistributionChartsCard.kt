package com.cebolao.lotofacil.ui.components.stats

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
        prepareData(stats, selectedPattern).toImmutableList()
    }
    val maxValue by remember(chartData) {
        derivedStateOf { (chartData.maxOfOrNull { it.second } ?: 0) }
    }

    val statsAnalysis by remember(chartData) {
        derivedStateOf { calculateWeightedMeanAndStdDev(chartData) }
    }
    val mean = statsAnalysis.first
    val stdDev = statsAnalysis.second

    val showNormalLine by remember(mean, stdDev, selectedPattern, chartData.size) {
        derivedStateOf {
            mean != null && stdDev != null && stdDev > 0.0 && chartData.size >= 3
        }
    }

    // Value of the last draw to highlight in the chart
    val highlightValue by remember(lastDraw, selectedPattern) {
        derivedStateOf {
            lastDraw?.let { draw ->
                val rawValue = calculatePatternValue(draw, selectedPattern)
                if (selectedPattern == StatisticPattern.SUM) {
                    ((rawValue / GameConstants.SUM_STEP) * GameConstants.SUM_STEP).toString()
                } else {
                    rawValue.toString()
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
    ) {
        // Pattern Selector - Outside the card to prevent overlap
        PatternSelector(
            selectedPattern = selectedPattern,
            onPatternSelected = onPatternSelected
        )

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            variant = CardVariant.Glass,
            outlined = true,
            title = stringResource(R.string.home_distribution_title),
            contentPadding = Dimen.Spacing16,
            headerActions = {
                // Highlight legend (last draw)
                val highlight = highlightValue
                if (highlight != null) {
                    Canvas(modifier = Modifier.size(Dimen.IndicatorHeightSmall)) {
                        drawCircle(color = scheme.error)
                    }
                    Text(
                        text = stringResource(R.string.distribution_last_label, highlight),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {

            // Chart Area - Ensure explicit height to avoid clipping axis
            BarChart(
                data = chartData.toImmutableList(),
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.BarChartHeight + Dimen.ChartAxisLabelPadding), // Extra space for rotated labels
                chartHeight = Dimen.BarChartHeight,
                chartType = ChartType.BAR,
                showNormalLine = showNormalLine,
                mean = mean?.toFloat(),
                stdDev = stdDev?.toFloat(),
                highlightValue = highlightValue
            )
            
            // Helpful spacer at the bottom
            Spacer(modifier = Modifier.height(Dimen.Spacing4))
        }
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

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(patterns.toList()) { pattern ->
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

    // For SUM, respect range and step from AppConfig
    if (pattern == StatisticPattern.SUM) {
        val buckets = (GameConstants.SUM_MIN_RANGE..GameConstants.SUM_MAX_RANGE step GameConstants.SUM_STEP)
            .associateWith { 0 }
            .toMutableMap()

        raw.forEach { (k, v) -> if (buckets.containsKey(k)) buckets[k] = v }

        return buckets.entries
            .sortedBy { it.key }
            .map { it.key.toString() to it.value }
    }

    // Other patterns: fill "gaps" to smooth reading
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

/** "Natural" discrete domains for consistent axes. */
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
internal fun DistributionChartsCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
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
