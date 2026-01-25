package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.presentation.viewmodel.GameSummary
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.util.Formatters

@Composable
fun GameSummaryCard(
    summary: GameSummary,
    modifier: Modifier = Modifier
) {
    if (summary.totalGames <= 0) return

    AppCard(
        modifier = modifier,
        outlined = true,
        variant = CardVariant.Glass,
        title = stringResource(R.string.games_summary_title)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                label = stringResource(R.string.games_summary_total_games),
                value = summary.totalGames.toString()
            )
            SummaryItem(
                label = stringResource(R.string.games_summary_pinned_games),
                value = summary.pinnedGames.toString()
            )
            SummaryItem(
                label = stringResource(R.string.games_summary_total_cost),
                value = Formatters.formatCurrency(summary.totalCost)
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
