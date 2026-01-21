package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.ui.components.layout.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.stats.DistributionChartsCard
import com.cebolao.lotofacil.ui.components.layout.EntryAnimation
import com.cebolao.lotofacil.ui.components.game.LastDrawCard
import com.cebolao.lotofacil.ui.components.game.NextContestHeroCard
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.components.stats.StatisticsPanel
import com.cebolao.lotofacil.ui.components.game.WelcomeCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.staggerDelay
import com.cebolao.lotofacil.ui.components.common.LoadingCard
import com.cebolao.lotofacil.ui.components.common.StandardAttentionCard
import com.cebolao.lotofacil.presentation.viewmodel.HomeScreenState
import com.cebolao.lotofacil.presentation.viewmodel.HomeUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.HomeUiState
import com.cebolao.lotofacil.presentation.viewmodel.HomeViewModel
import com.cebolao.lotofacil.ui.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    listState: LazyListState,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.syncMessageRes?.let { msgId ->
        val msg = stringResource(msgId)
        LaunchedEffect(msgId) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(HomeUiEvent.OnMessageShown)
        }
    }

    HomeScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateToChecker = { numbers ->
            navController?.navigateToChecker(numbers)
        },
        onNavigateToResults = {
            navController?.navigate(com.cebolao.lotofacil.navigation.ResultsRoute)
        },
        listState = listState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HomeUiEvent) -> Unit,
    onNavigateToChecker: (Set<Int>) -> Unit,
    onNavigateToResults: () -> Unit,
    listState: LazyListState
    ) {
    val title = stringResource(R.string.app_name)
    val subtitle = stringResource(R.string.home_subtitle)
    val appScreenContent = @Composable { innerPadding: androidx.compose.foundation.layout.PaddingValues ->
        val isRefreshing = remember(uiState.isSyncing, uiState.screenState) {
            uiState.isSyncing || uiState.screenState is HomeScreenState.Loading
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onEvent(HomeUiEvent.ForceSync) },
            modifier = Modifier.fillMaxSize()
        ) {
            val successState = uiState.screenState as? HomeScreenState.Success
            StandardPageLayout(
                scaffoldPadding = innerPadding,
                listState = listState
            ) {
                item(key = "welcome") {
                    AnimateOnEntry(
                        delayMillis = staggerDelay(0).toLong(),
                        animation = EntryAnimation.Fade
                    ) {
                        WelcomeCard()
                    }
                }

                item(key = "hero") {
                    AnimateOnEntry(
                        delayMillis = staggerDelay(1).toLong(),
                        animation = EntryAnimation.Scale
                    ) {
                        when {
                            successState != null -> NextContestHeroCard(successState.nextDrawInfo)
                            uiState.screenState is HomeScreenState.Loading -> LoadingCard()
                        }
                    }
                }

                (uiState.screenState as? HomeScreenState.Error)?.let { error ->
                    item(key = "error") {
                        AnimateOnEntry(
                            delayMillis = staggerDelay(2).toLong(),
                            animation = EntryAnimation.SlideUp
                        ) {
                            StandardAttentionCard(
                                title = stringResource(R.string.general_error_title),
                                message = stringResource(error.messageResId),
                                icon = AppIcons.Error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                successState?.lastDraw?.let { draw ->
                    item(key = "last_draw") {
                        AnimateOnEntry(
                            delayMillis = staggerDelay(3).toLong(),
                            animation = EntryAnimation.SlideUp
                        ) {
                            LastDrawSection(
                                draw = draw,
                                details = successState.details,
                                onCheckGame = onNavigateToChecker,
                                onRefresh = { onEvent(HomeUiEvent.ForceSync) },
                                onNavigateToResults = onNavigateToResults
                            )
                        }
                    }
                }

                val stats = uiState.statistics
                if (stats != null) {
                    item(key = "stats_panel") {
                        AnimateOnEntry(
                            delayMillis = staggerDelay(4).toLong(),
                            animation = EntryAnimation.SlideUp
                        ) {
                            StatisticsPanel(
                                stats = stats,
                                modifier = Modifier.fillMaxWidth(),
                                onTimeWindowSelected = { onEvent(HomeUiEvent.OnTimeWindowSelected(it)) },
                                selectedWindow = uiState.selectedTimeWindow,
                                isStatsLoading = uiState.isStatsLoading
                            )
                        }
                    }

                    item(key = "distribution_charts") {
                        AnimateOnEntry(
                            delayMillis = staggerDelay(5).toLong(),
                            animation = EntryAnimation.SlideUp
                        ) {
                            DistributionChartsCard(
                                stats = stats,
                                selectedPattern = uiState.selectedPattern,
                                onPatternSelected = { onEvent(HomeUiEvent.OnPatternSelected(it)) },
                                lastDraw = successState?.lastDraw,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item(key = "disclaimer") {
                    AnimateOnEntry(
                        delayMillis = staggerDelay(6).toLong(),
                        animation = EntryAnimation.Fade
                    ) {
                        StandardAttentionCard(
                            title = stringResource(R.string.attention_title),
                            message = stringResource(R.string.attention_message),
                            modifier = Modifier.padding(top = Dimen.ItemSpacing)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section displaying the last draw with navigation to checker and results.
 * Follows design system spacing and component patterns.
 */
@Composable
private fun LastDrawSection(
    draw: com.cebolao.lotofacil.ui.model.UiDraw,
    details: com.cebolao.lotofacil.ui.model.UiDrawDetails?,
    onCheckGame: (Set<Int>) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToResults: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8) // Reduzido de Medium para 8dp
    ) {
        LastDrawCard(
            draw = draw,
            details = details,
            onCheckGame = onCheckGame,
            onRefresh = onRefresh
        )
        
        TextButton(
            onClick = onNavigateToResults,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(R.string.results_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
