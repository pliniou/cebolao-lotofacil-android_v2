package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.model.StatisticsReport
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun DistributionChartsCard(
    stats: StatisticsReport,
    selectedPattern: StatisticPattern,
    onPatternSelected: (StatisticPattern) -> Unit,
    lastDraw: com.cebolao.lotofacil.domain.model.Draw? = null,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val chartData = remember(selectedPattern, stats) { prepareData(stats, selectedPattern) }
    val maxValue = remember(chartData) { chartData.maxOfOrNull { it.second } ?: 0 }
    val (mean, stdDev) = remember(chartData) { calculateWeightedMeanAndStdDev(chartData) }
    val showNormalLine = mean != null && stdDev != null && stdDev > 0.0

    // Calcule o valor do último sorteio para o padrão selecionado
    val highlightValue = remember(lastDraw, selectedPattern) {
        lastDraw?.let { draw ->
            calculatePatternValue(draw, selectedPattern).toString()
        }
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Solid,
        color = scheme.surfaceContainer,
        title = stringResource(R.string.edu_section_filters),
        headerActions = {
             // Legenda simples se houver destaque
             if (highlightValue != null) {
                 Canvas(modifier = Modifier.size(8.dp)) {
                     drawCircle(color = scheme.error)
                 }
                 Text(
                     text = "Último: $highlightValue",
                     style = MaterialTheme.typography.labelSmall,
                     color = scheme.onSurfaceVariant
                 )
             }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(StatisticPattern.entries) { pattern ->
                    CustomChip(
                        selected = selectedPattern == pattern,
                        onClick = { onPatternSelected(pattern) },
                        label = pattern.title
                    )
                }
            }

            // Gráfico (mantém compatibilidade com BarChart)
            BarChart(
                data = chartData.toImmutableList(),
                maxValue = maxValue,
                showNormalLine = showNormalLine,
                mean = mean?.toFloat(),
                stdDev = stdDev?.toFloat(),
                highlightValue = highlightValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.BarChartHeight)
            )
        }
    }
}

private fun calculatePatternValue(draw: com.cebolao.lotofacil.domain.model.Draw, pattern: StatisticPattern): Int {
    return when (pattern) {
        StatisticPattern.SUM -> draw.sum
        StatisticPattern.EVENS -> draw.evens
        StatisticPattern.PRIMES -> draw.primes
        StatisticPattern.FRAME -> draw.frame
        StatisticPattern.FIBONACCI -> draw.fibonacci
        StatisticPattern.LINES -> draw.lines
        StatisticPattern.COLUMNS -> draw.columns
        StatisticPattern.SEQUENCES -> draw.sequences
        StatisticPattern.QUADRANTS -> draw.quadrants
    }
}

private fun calculateWeightedMeanAndStdDev(
    data: List<Pair<String, Int>>
): Pair<Double?, Double?> {
    val numeric = data.mapNotNull { (label, count) ->
        label.toIntOrNull()?.let { it to count }
    }

    if (numeric.isEmpty()) return null to null

    val total = numeric.sumOf { it.second }
    if (total <= 0) return null to null

    val weightedSum = numeric.sumOf { (x, count) -> x * count.toDouble() }
    val mean = weightedSum / total

    val variance = numeric.sumOf { (x, count) ->
        count * (x - mean).pow(2)
    } / total

    val stdDev = sqrt(variance)
    return mean to stdDev
}

private fun prepareData(
    stats: StatisticsReport,
    pattern: StatisticPattern
): List<Pair<String, Int>> {
    val raw: Map<Int, Int> = when (pattern) {
        StatisticPattern.SUM -> stats.sumDistribution
        StatisticPattern.EVENS -> stats.evenDistribution
        StatisticPattern.PRIMES -> stats.primeDistribution
        StatisticPattern.FRAME -> stats.frameDistribution
        StatisticPattern.FIBONACCI -> stats.fibonacciDistribution
        StatisticPattern.LINES -> stats.linesDistribution
        StatisticPattern.COLUMNS -> stats.columnsDistribution
        StatisticPattern.SEQUENCES -> stats.sequencesDistribution
        StatisticPattern.QUADRANTS -> stats.quadrantsDistribution
    }

    if (raw.isEmpty()) return emptyList()

    // SUM: mantém o range e step definidos no AppConfig (compatibilidade do projeto)
    if (pattern == StatisticPattern.SUM) {
        val buckets =
            (GameConstants.SUM_MIN_RANGE..GameConstants.SUM_MAX_RANGE step GameConstants.SUM_STEP)
                .associateWith { 0 }
                .toMutableMap()

        raw.forEach { (k, v) ->
            if (buckets.containsKey(k)) buckets[k] = v
        }

        return buckets.entries
            .sortedBy { it.key }
            .map { it.key.toString() to it.value }
    }

    // Para padrões discretos, preencher “buracos” melhora a leitura do histograma
    // e evita efeitos visuais inconsistentes (inclusive na linha normal).
    val domain = domainFor(pattern)
    val (minKey, maxKey) = (raw.keys.minOrNull() ?: 0) to (raw.keys.maxOrNull() ?: 0)
    val rangeToUse: IntRange = domain ?: (minKey..maxKey)
    val buckets = rangeToUse
        .associateWith { 0 }
        .toMutableMap()

    raw.forEach { (k, v) ->
        // Não descarta dados inesperados; se estiver fora do range, adiciona depois.
        if (buckets.containsKey(k)) buckets[k] = v
    }

    val inRange = buckets.entries
        .sortedBy { it.key }
        .map { it.key.toString() to it.value }

    // Se houver chaves fora do range (raríssimo), mantém no final para não “sumir” dados.
    val outOfRange = raw.entries
        .filter { !rangeToUse.contains(it.key) }
        .sortedBy { it.key }
        .map { it.key.toString() to it.value }

    return inRange + outOfRange
}

/**
 * Domínios discretos “naturais” (teóricos) para manter consistência de eixo X.
 * Evita que a UI varie conforme lacunas do histórico.
 */
private fun domainFor(pattern: StatisticPattern): IntRange? = when (pattern) {
    StatisticPattern.EVENS -> 0..15
    StatisticPattern.PRIMES -> 0..9
    StatisticPattern.FRAME -> 0..15
    StatisticPattern.FIBONACCI -> 0..7
    StatisticPattern.LINES -> 0..5
    StatisticPattern.COLUMNS -> 0..5
    StatisticPattern.QUADRANTS -> 0..4
    StatisticPattern.SEQUENCES -> null
    StatisticPattern.SUM -> null
}
