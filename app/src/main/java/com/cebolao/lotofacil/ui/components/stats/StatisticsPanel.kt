package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.ui.model.UiStatisticsReport
import com.cebolao.lotofacil.ui.components.common.AppDivider
import com.cebolao.lotofacil.ui.components.common.CustomChip
import com.cebolao.lotofacil.ui.components.game.NumberBall
import com.cebolao.lotofacil.ui.components.game.NumberBallSize
import com.cebolao.lotofacil.ui.components.game.NumberBallVariant
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
private data class PreparedStatsData(
    val hotNumbers: ImmutableList<Pair<Int, Int>>,
    val overdueNumbers: ImmutableList<Pair<Int, Int>>
)

private const val PHONE_BREAKPOINT_DP = 600

@Composable
fun StatisticsPanel(
    stats: UiStatisticsReport,
    modifier: Modifier = Modifier,
    onTimeWindowSelected: (Int) -> Unit,
    selectedWindow: Int,
    isStatsLoading: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Solid,
        color = scheme.surfaceContainer,
        outlined = true,
        hasBorder = true,
        title = stringResource(R.string.home_statistics_center),
        headerActions = {
            AnimatedVisibility(
                visible = isStatsLoading,
                enter = scaleIn(animationSpec = Motion.Spring.snappy()) + fadeIn(animationSpec = Motion.Tween.enter()),
                exit = scaleOut(animationSpec = Motion.Spring.gentle()) + fadeOut(animationSpec = Motion.Tween.exit())
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.IconSmall),
                    strokeWidth = Dimen.Border.Thin,
                    color = scheme.secondary
                )
            }
        },
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort),
            modifier = Modifier.animateContentSize(animationSpec = Motion.Spring.gentle())
        ) {
            TimeWindowSelector(
                selectedWindow = selectedWindow,
                onTimeWindowSelected = onTimeWindowSelected,
                onShowMessage = { message ->
                    android.widget.Toast
                        .makeText(context, message, android.widget.Toast.LENGTH_SHORT)
                        .show()
                },
                enabled = !isStatsLoading
            )

            AppDivider()

            AnimatedContent(
                targetState = isStatsLoading,
                transitionSpec = {
                    (fadeIn(animationSpec = Motion.Tween.enter()) + expandVertically())
                        .togetherWith(fadeOut(animationSpec = Motion.Tween.exit()) + shrinkVertically())
                        .using(SizeTransform(clip = false))
                },
                label = "StatsContentTransition"
            ) { loading ->
                if (!loading) {
                    StatsContent(stats = stats)
                } else {
                    Spacer(modifier = Modifier.height(Dimen.SectionSpacing))
                }
            }
        }
    }
}

@Composable
private fun TimeWindowSelector(
    selectedWindow: Int,
    onTimeWindowSelected: (Int) -> Unit,
    onShowMessage: (String) -> Unit,
    enabled: Boolean
) {
    val scheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
        Text(
            text = stringResource(R.string.home_analysis_period),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.6f)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing4),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(
                items = GameConstants.TIME_WINDOWS,
                key = { it },
                contentType = { "time_window" }
            ) { window ->
                val label = when (window) {
                    0 -> stringResource(R.string.stats_time_window_all)
                    else -> stringResource(R.string.stats_time_window_last, window)
                }
                val selectionMessage = if (window == 0) {
                    stringResource(R.string.stats_period_all_selected)
                } else {
                    pluralStringResource(R.plurals.stats_period_selected, window, window)
                }

                CustomChip(
                    selected = window == selectedWindow,
                    onClick = {
                        onTimeWindowSelected(window)
                        onShowMessage(selectionMessage)
                    },
                    label = label,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun StatsContent(stats: UiStatisticsReport) {
    val preparedData = remember(stats) {
        PreparedStatsData(
            hotNumbers = stats.mostFrequentNumbers.map { it.number to it.frequency }.toImmutableList(),
            overdueNumbers = stats.mostOverdueNumbers.map { it.number to it.frequency }.toImmutableList()
        )
    }

    @Suppress("ConfigurationScreenWidthHeight")
    val configuration = LocalConfiguration.current
    
    // Use derivedStateOf for configuration-based layout decision to optimize recompositions
    val useColumnLayout by remember(configuration.screenWidthDp) {
        derivedStateOf {
            configuration.screenWidthDp < PHONE_BREAKPOINT_DP
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SpacingTiny)) {
        if (useColumnLayout) {
            StatRow(
                title = stringResource(R.string.home_hot_numbers),
                data = preparedData.hotNumbers,
                highlightColor = primary,
                unit = stringResource(R.string.stats_unit_times)
            )
            StatRow(
                title = stringResource(R.string.home_overdue_numbers),
                data = preparedData.overdueNumbers,
                highlightColor = error,
                isOverdue = true,
                unit = stringResource(R.string.stats_unit_contests)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
                ) {
                    StatRow(
                        title = stringResource(R.string.home_hot_numbers),
                        data = preparedData.hotNumbers,
                        highlightColor = primary,
                        unit = stringResource(R.string.stats_unit_times)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
                ) {
                    StatRow(
                        title = stringResource(R.string.home_overdue_numbers),
                        data = preparedData.overdueNumbers,
                        highlightColor = error,
                        isOverdue = true,
                        unit = stringResource(R.string.stats_unit_contests)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    title: String,
    data: ImmutableList<Pair<Int, Int>>,
    highlightColor: androidx.compose.ui.graphics.Color,
    unit: String,
    isOverdue: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        outlined = false,
        color = scheme.surfaceContainerLow,
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )

            if (data.isEmpty()) {
                AppCard(
                    modifier = Modifier.fillMaxWidth().padding(Dimen.SpacingTiny),
                    outlined = true,
                    color = scheme.surface,
                    contentPadding = Dimen.SpacingShort
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = Dimen.SpacingShort)
                    ) {
                        Text(
                            text = stringResource(R.string.stats_no_data_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.stats_no_data_message),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingTiny),
                    contentPadding = PaddingValues(horizontal = Dimen.SpacingShort)
                ) {
                    items(
                        items = data,
                        key = { it.first },
                        contentType = { "stat_item" }
                    ) { (number, value) ->
                        StatItem(
                            number = number,
                            value = value,
                            highlightColor = highlightColor,
                            isOverdue = isOverdue,
                            unit = unit,
                            modifier = Modifier.widthIn(min = Dimen.TableColumnWidthLarge)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    number: Int,
    value: Int,
    highlightColor: androidx.compose.ui.graphics.Color,
    isOverdue: Boolean,
    unit: String,
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier,
        outlined = false,
        color = scheme.surfaceContainerHigh,
        contentPadding = Dimen.SpacingShort
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
            modifier = Modifier.fillMaxWidth()
        ) {
            NumberBall(
                number = number,
                sizeVariant = NumberBallSize.Medium,
                variant = if (isOverdue) NumberBallVariant.Neutral else NumberBallVariant.Primary
            )

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = highlightColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsPanelPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            StatisticsPanel(
                stats = UiStatisticsReport(
                    mostFrequentNumbers = emptyList(),
                    mostOverdueNumbers = emptyList(),
                    evenDistribution = mapOf(7 to 5, 8 to 10),
                    primeDistribution = mapOf(5 to 8),
                    frameDistribution = mapOf(8 to 12),
                    sumDistribution = mapOf(200 to 15),
                    fibonacciDistribution = mapOf(),
                    multiplesOf3Distribution = mapOf(),
                    centerDistribution = mapOf(),
                    sequencesDistribution = mapOf(),
                    averageSum = 195f,
                    totalDrawsAnalyzed = 100,
                    analysisDate = "01/01/2026"
                ),
                onTimeWindowSelected = {},
                selectedWindow = 0,
                isStatsLoading = false
            )
        }
    }
}
