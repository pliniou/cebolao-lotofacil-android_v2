package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.presentation.viewmodel.GameAnalysisUiState
import com.cebolao.lotofacil.ui.components.stats.FinancialPerformanceCard
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.GlassCard

@Composable
fun GameAnalysisSheetContent(
    analysisState: GameAnalysisUiState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimen.ScreenPadding,
                end = Dimen.ScreenPadding,
                bottom = Dimen.SectionSpacing
            ),
        verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.checker_performance_analysis),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = AppIcons.CloseOutlined,
                    contentDescription = stringResource(R.string.general_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimen.ItemSpacing))

        when (analysisState) {
            is GameAnalysisUiState.Idle -> Unit
            is GameAnalysisUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.SectionSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.LoadingIndicatorSize),
                        strokeWidth = Dimen.Border.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is GameAnalysisUiState.Error -> {
                Text(
                    text = stringResource(analysisState.messageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is GameAnalysisUiState.Success -> {
                Text(
                    text = analysisState.result.game.numbers.sorted()
                        .joinToString(" - ") { it.toString().padStart(2, '0') },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FinancialPerformanceCard(
                    report = analysisState.result.checkReport,
                    modifier = Modifier.fillMaxWidth()
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                     Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.CardContentPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                     ) {
                        Text(
                            text = stringResource(R.string.games_analysis_stats_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (analysisState.result.simpleStats.size > 8) {
                            LazyColumn(modifier = Modifier.height(200.dp)) {
                                items(analysisState.result.simpleStats.size) { idx ->
                                    val pair = analysisState.result.simpleStats[idx]
                                    StatRow(pair.first, pair.second)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
                                analysisState.result.simpleStats.forEach { pair ->
                                    StatRow(pair.first, pair.second)
                                }
                            }
                        }
                     }
                }
            }
        }
    }
}

@Composable
private fun StatRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
