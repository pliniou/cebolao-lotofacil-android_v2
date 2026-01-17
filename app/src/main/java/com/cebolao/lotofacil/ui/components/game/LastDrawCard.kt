package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.WinnerLocation
import com.cebolao.lotofacil.ui.components.common.AppTable
import com.cebolao.lotofacil.ui.components.common.AppTableData
import com.cebolao.lotofacil.ui.components.common.AppTableStyle
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.model.UiDraw
import com.cebolao.lotofacil.ui.model.UiDrawDetails
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.Formatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LastDrawCard(
    draw: UiDraw,
    details: UiDrawDetails?,
    modifier: Modifier = Modifier,
    onCheckGame: (Set<Int>) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier.fillMaxWidth(),
        outlined = true,
        contentPadding = Dimen.Spacing12
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_result_title, draw.contestNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                DrawDateSubtitle(draw)
            }

            // Numbers
            ModernNumberGrid(draw.numbers)

            // Stats (tonal surface, flat)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = scheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = scheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(Dimen.Border.Thin, scheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimen.CardContentPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
                ) {
                    Text(
                        text = stringResource(R.string.statistics),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    DrawStatisticsSection(draw)
                }
            }

            // Primary action (flat, modern)
            FilledTonalButton(
                onClick = { onCheckGame(draw.numbers) },
                modifier = Modifier
                    .height(Dimen.ActionButtonHeight)
                    .fillMaxWidth(0.8f),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer
                ),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(Dimen.IconSmall)
                )
                Spacer(Modifier.width(Dimen.Spacing8))
                Text(
                    text = stringResource(R.string.action_test),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Details
            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

            if (details != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
                ) {
                    PrizeTableSection(details)
                    LocationInfoSection(details)
                    if (details.winnersByState.isNotEmpty()) {
                        WinnersByStateSection(details.winnersByState)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.Spacing8),
                    horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = Dimen.Border.Thick,
                        color = scheme.primary
                    )
                    Text(
                        text = stringResource(R.string.loading_details_fallback),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawDateSubtitle(draw: UiDraw) {
    val dateText = remember(draw.date) {
        draw.date?.let { millis ->
            val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            formatter.format(Date(millis))
        }.orEmpty()
    }

    if (dateText.isNotEmpty()) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModernNumberGrid(numbers: Set<Int>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing4, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4),
        maxItemsInEachRow = 5
    ) {
        numbers.sorted().forEach { number ->
            NumberBall(
                number = number,
                sizeVariant = NumberBallSize.Medium,
                variant = NumberBallVariant.Primary
            )
        }
    }
}

@Composable
private fun DrawStatisticsSection(draw: UiDraw) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
    ) {
        StatItem(
            label = stringResource(R.string.stat_sum),
            value = draw.sum.toString(),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = stringResource(R.string.stat_evens),
            value = draw.evens.toString(),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = stringResource(R.string.stat_odds),
            value = draw.odds.toString(),
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
    ) {
        StatItem(
            label = stringResource(R.string.stat_primes),
            value = draw.primes.toString(),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = stringResource(R.string.stat_sequences),
            value = draw.sequences.toString(),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = stringResource(R.string.stat_fibonacci),
            value = draw.fibonacci.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        color = scheme.surface,
        contentColor = scheme.onSurface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.Spacing8, vertical = Dimen.Spacing8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingTiny)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.primary
            )
        }
    }
}

@Composable
private fun PrizeTableSection(details: UiDrawDetails) {
    Text(
        text = stringResource(R.string.prize_table_title),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = Dimen.Spacing4)
    )

    val headers = listOf(
        stringResource(R.string.prize_table_header_prize),
        stringResource(R.string.prize_table_header_winners),
        stringResource(R.string.prize_table_header_amount)
    )

    val rows = details.prizeRates.map { rate ->
        listOf(
            rate.description,
            rate.winnerCount.toString(),
            Formatters.formatCurrency(rate.prizeValue)
        )
    }

    AppTable(
        data = AppTableData(
            headers = headers,
            rows = rows,
            weights = listOf(1f, 0.55f, 1f),
            textAligns = listOf(TextAlign.Start, TextAlign.Center, TextAlign.End)
        ),
        style = AppTableStyle(showDividers = false)
    )
}

@Composable
private fun LocationInfoSection(details: UiDrawDetails) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceVariant,
        contentColor = scheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.Spacing16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
        ) {
            Text(
                text = stringResource(R.string.draw_location_label),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = details.location ?: stringResource(R.string.location_not_informed),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WinnersByStateSection(winners: List<WinnerLocation>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.winners_by_state),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = Dimen.Spacing4)
        )

        val headers = listOf(
            stringResource(R.string.uf),
            stringResource(R.string.city),
            stringResource(R.string.count_short)
        )

        val displayWinners = winners.take(5)
        val rows = displayWinners.map { winner ->
            listOf(winner.state, winner.city, winner.count.toString())
        }

        AppTable(
            data = AppTableData(
                headers = headers,
                rows = rows,
                weights = listOf(0.18f, 0.64f, 0.18f),
                textAligns = listOf(TextAlign.Start, TextAlign.Start, TextAlign.End)
            ),
            style = AppTableStyle(showDividers = false)
        )

        if (winners.size > 5) {
            Text(
                text = stringResource(R.string.and_more_count, winners.size - 5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = Dimen.Spacing8)
            )
        }
    }
}
