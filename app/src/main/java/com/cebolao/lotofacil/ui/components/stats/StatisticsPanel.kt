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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Solid,
        color = scheme.surfaceContainer,
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
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium),
            modifier = Modifier.animateContentSize(animationSpec = Motion.Spring.gentle())
        ) {
            TimeWindowSelector(
                selectedWindow = selectedWindow,
                onTimeWindowSelected = onTimeWindowSelected,
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
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(GameConstants.TIME_WINDOWS) { window ->
                val label = when (window) {
                    0 -> stringResource(R.string.home_all_contests)
                    else -> stringResource(R.string.home_last_contests_format, window)
                }

                CustomChip(
                    selected = window == selectedWindow,
                    onClick = { onTimeWindowSelected(window) },
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

    val configuration = LocalConfiguration.current
    val useColumnLayout = configuration.screenWidthDp < PHONE_BREAKPOINT_DP

    val tertiary = MaterialTheme.colorScheme.tertiary
    val error = MaterialTheme.colorScheme.error

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing)) {
        if (useColumnLayout) {
            StatRow(
                title = stringResource(R.string.home_hot_numbers),
                data = preparedData.hotNumbers,
                highlightColor = tertiary
            )
            StatRow(
                title = stringResource(R.string.home_overdue_numbers),
                data = preparedData.overdueNumbers,
                highlightColor = error,
                isOverdue = true
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
                        highlightColor = tertiary
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
                        isOverdue = true
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
    isOverdue: Boolean = false
) {
    if (data.isEmpty()) return

    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        outlined = false,
        color = scheme.surfaceVariant,
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingTiny),
                contentPadding = PaddingValues(horizontal = Dimen.SpacingShort)
            ) {
                items(data) { (number, value) ->
                    StatItem(
                        number = number,
                        value = value,
                        highlightColor = highlightColor,
                        isOverdue = isOverdue,
                        modifier = Modifier.widthIn(min = 80.dp)
                    )
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
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier,
        outlined = false,
        color = scheme.surface,
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
                text = if (isOverdue) "dias" else "vezes",
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
        Column(modifier = Modifier.padding(16.dp)) {
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
