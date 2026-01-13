package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.RangeStatistics
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun RangeAnalysisCard(
    rangeStats: RangeStatistics,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        title = stringResource(R.string.checker_range_analysis_title),
        outlined = true
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            // Header com estatísticas gerais
            RangeStatsHeader(rangeStats)
            
            // Distribuição por faixa
            RangeDistributionGrid(rangeStats)
            
            // Insights
            RangeInsights(rangeStats)
        }
    }
}

@Composable
private fun RangeStatsHeader(stats: RangeStatistics) {
    val scheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.checker_total_analyzed),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = "${stats.totalDraws}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
        }
        
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(
                text = stringResource(R.string.checker_avg_hits),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = String.format("%.1f", stats.averageHits),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
        }
    }
}

@Composable
private fun RangeDistributionGrid(stats: RangeStatistics) {
    val scheme = MaterialTheme.colorScheme
    
    Text(
        text = stringResource(R.string.checker_distribution_by_range),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = scheme.onSurface,
        modifier = Modifier.padding(bottom = Dimen.Spacing8)
    )
    
    // Grid 2x2 para as faixas principais
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            RangeItem(
                title = stringResource(R.string.checker_range_0_5),
                count = stats.range0to5,
                percentage = (stats.range0to5.toFloat() / stats.totalDraws * 100),
                color = scheme.primary
            )
            RangeItem(
                title = stringResource(R.string.checker_range_6_10),
                count = stats.range6to10,
                percentage = (stats.range6to10.toFloat() / stats.totalDraws * 100),
                color = scheme.secondary
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            RangeItem(
                title = stringResource(R.string.checker_range_11_15),
                count = stats.range11to15,
                percentage = (stats.range11to15.toFloat() / stats.totalDraws * 100),
                color = scheme.tertiary
            )
            RangeItem(
                title = stringResource(R.string.checker_range_16_20),
                count = stats.range16to20,
                percentage = (stats.range16to20.toFloat() / stats.totalDraws * 100),
                color = scheme.error
            )
        }
    }
}

@Composable
private fun RangeItem(
    title: String,
    count: Int,
    percentage: Float,
    color: androidx.compose.ui.graphics.Color
) {
    val scheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimen.Spacing8),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$count sorteios",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface
            )
            Text(
                text = String.format("%.1f%%", percentage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun RangeInsights(stats: RangeStatistics) {
    val scheme = MaterialTheme.colorScheme
    
    // Encontrar a faixa mais comum
    val ranges = listOf(
        Triple("0-5", stats.range0to5, scheme.primary),
        Triple("6-10", stats.range6to10, scheme.secondary),
        Triple("11-15", stats.range11to15, scheme.tertiary),
        Triple("16-20", stats.range16to20, scheme.error)
    )
    
    val mostCommon = ranges.maxByOrNull { it.second }
    val leastCommon = ranges.minByOrNull { it.second }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
    ) {
        mostCommon?.let { (range, count, color) ->
            Text(
                text = stringResource(R.string.checker_most_common_range, range, count),
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        leastCommon?.let { (range, count, _) ->
            Text(
                text = stringResource(R.string.checker_least_common_range, range, count),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}
