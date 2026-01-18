package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.CheckResult
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.StackSans
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

@Composable
fun CheckResultCard(
    result: CheckResult,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = true
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
        ) {
            val totalWins = result.scoreCounts.values.sum()
            ResultHeader(totalWins, result.lastCheckedContest)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimen.Spacing8),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER)
            )

            
            if (result.recentHits.isNotEmpty()) {
                val chartData = remember(result.recentHits) {
                    result.recentHits.map { it.first.toString() to it.second }.toImmutableList()
                }
                // Strict limit of last 12 for UI
                val condensedData = remember(chartData) { chartData.takeLast(12).toImmutableList() }
                
                // Chart specific data: format labels to last 3 digits for readability
                val barChartData = remember(condensedData) {
                    condensedData.map { (label, value) -> 
                        val shortLabel = if (label.length > 3) label.takeLast(3) else label
                        shortLabel to value
                    }.toImmutableList()
                }

                var isChartMode by rememberSaveable { mutableStateOf(true) }

                Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
                    // Header + Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.checker_recent_hits_chart_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                        ) {
                             TextButton(
                                onClick = { isChartMode = true },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = if (isChartMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.checker_view_chart),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isChartMode) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            TextButton(
                                onClick = { isChartMode = false },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = if (!isChartMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.checker_view_table),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (!isChartMode) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    if (isChartMode) {
                        BarChart(
                            data = barChartData,
                            maxValue = GameConstants.GAME_SIZE,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimen.ChartHeightSmall),
                            chartHeight = Dimen.ChartHeightSmall,
                            chartType = ChartType.BAR,
                            highlightPredicate = { it >= GameConstants.MIN_PRIZE_SCORE }
                        )
                    } else {
                         // Table View (Newest first)
                        val tableRows = remember(condensedData) {
                            condensedData.reversed().map { (contest, hits) ->
                                listOf(contest, hits.toString())
                            }
                        }
                        
                        com.cebolao.lotofacil.ui.components.common.AppTable(
                            data = com.cebolao.lotofacil.ui.components.common.AppTableData(
                                headers = listOf(
                                    stringResource(R.string.results_contest_number_format, 0).split(" ")[0], // "Concurso" generic
                                    stringResource(R.string.checker_score_breakdown_hits_format, 0).split(" ")[1].replaceFirstChar { it.uppercase() } // "Pontos/Acertos"
                                ),
                                rows = tableRows,
                                weights = listOf(1f, 1f),
                                textAligns = listOf(TextAlign.Start, TextAlign.End)
                            ),
                            style = com.cebolao.lotofacil.ui.components.common.AppTableStyle(
                                showDividers = true
                            )
                        )
                    }

                    // Footnote
                    Text(
                        text = stringResource(R.string.checker_showing_last_12),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimen.Spacing8),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER)
                )
            }

            if (totalWins > 0) {
                (15 downTo 11).forEach { score ->
                    val count = result.scoreCounts[score] ?: 0
                    ScoreRow(score, count)
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimen.Spacing8),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Alpha.DIVIDER)
                )

                LastHit(result)
            } else {
                NoWins()
            }
        }
    }
}

@Composable
private fun ResultHeader(wins: Int, checked: Int) {
    val hasWins = wins > 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
    ) {
        Icon(
            imageVector = if (hasWins) AppIcons.StarFilled else AppIcons.Analytics,
            contentDescription = null,
            tint = if (hasWins) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimen.IconLarge)
        )
        Column {
            Text(
                text = if (hasWins) {
                    stringResource(R.string.checker_results_header_wins)
                } else {
                    stringResource(R.string.checker_results_header_no_wins)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasWins) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.checker_results_analysis_in_contests,
                    checked
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ScoreRow(score: Int, count: Int) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(1000),
        label = "scoreCount"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.checker_score_breakdown_hits_format, score),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$animatedCount",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = StackSans,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (animatedCount == 1) " vez" else " vezes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
private fun LastHit(res: CheckResult) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
    ) {
        Icon(
            imageVector = AppIcons.Success,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(Dimen.IconSmall)
        )
        Text(
            text = if (res.lastHitContest != null && res.lastHitScore != null) {
                stringResource(
                    R.string.checker_last_hit_details_format,
                    res.lastHitContest,
                    res.lastHitScore
                )
            } else {
                "Nenhuma premiação encontrada"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (res.lastHitContest != null) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoWins() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.Spacing16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = AppIcons.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Dimen.IconMedium)
        )
        Text(
            text = stringResource(R.string.checker_no_wins_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = Dimen.Spacing12)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckResultCardPreview_Wins() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16).verticalScroll(rememberScrollState())) {
            CheckResultCard(
                result = CheckResult(
                    lastCheckedContest = 100,
                    scoreCounts = mapOf(11 to 10, 12 to 5, 13 to 2, 14 to 1, 15 to 0).toImmutableMap(),
                    recentHits = listOf(100 to 11, 98 to 12, 95 to 11).toImmutableList(),
                    lastHitContest = 100,
                    lastHitScore = 11
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckResultCardPreview_NoWins() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            CheckResultCard(
                result = CheckResult(
                    lastCheckedContest = 20,
                    scoreCounts = emptyMap<Int, Int>().toImmutableMap(),
                    recentHits = emptyList<Pair<Int, Int>>().toImmutableList(),
                    lastHitContest = null,
                    lastHitScore = null
                )
            )
        }
    }
}
