@file:OptIn(ExperimentalMaterial3Api::class)

package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.presentation.viewmodel.ResultsUiState
import com.cebolao.lotofacil.presentation.viewmodel.ResultsViewModel
import com.cebolao.lotofacil.ui.components.common.LoadingCard
import com.cebolao.lotofacil.ui.components.common.StandardAttentionCard
import com.cebolao.lotofacil.ui.components.game.NumberBall
import com.cebolao.lotofacil.ui.components.game.NumberBallSize
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.Formatters
import java.time.format.FormatStyle

@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    AppScreen(
        title = stringResource(R.string.results_title),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = stringResource(R.string.general_back)
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refreshHistory,
            modifier = Modifier.fillMaxSize()
        ) {
            StandardPageLayout(scaffoldPadding = innerPadding) {
                when (val state = uiState) {
                    is ResultsUiState.Loading -> {
                        item {
                            LoadingCard(
                                title = stringResource(R.string.results_title),
                                description = stringResource(R.string.loading_details_fallback)
                            )
                        }
                    }

                    is ResultsUiState.Error -> {
                        item {
                            StandardAttentionCard(
                                title = stringResource(R.string.general_error_title),
                                message = stringResource(R.string.results_error_message),
                                icon = AppIcons.Error
                            )
                        }
                    }

                    is ResultsUiState.Success -> {
                        items(
                            items = state.draws.sortedByDescending { it.contestNumber },
                            key = { it.contestNumber },
                            contentType = { "draw_item" }
                        ) { draw ->
                            DrawListItem(draw = draw)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawListItem(draw: Draw) {
    val contestLabel = stringResource(R.string.results_contest_number_format, draw.contestNumber)
    val dateText = rememberDrawDate(draw.date)
    val numbers = remember(draw.numbers) { draw.numbers.sorted() }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        outlined = true,
        contentPadding = Dimen.SpacingMedium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contestLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                if (dateText.isNotEmpty()) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.BallSpacing)
            ) {
                numbers.forEach { number ->
                    NumberBall(
                        number = number,
                        sizeVariant = NumberBallSize.Small
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberDrawDate(timestamp: Long?): String {
    return remember(timestamp) {
        if (timestamp == null || timestamp <= 0L) {
            ""
        } else {
            Formatters.formatDateMillis(timestamp, FormatStyle.SHORT)
        }
    }
}
