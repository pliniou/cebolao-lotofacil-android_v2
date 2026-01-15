package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HomeUiEvent) -> Unit,
    onNavigateToChecker: (Set<Int>) -> Unit
) {
    AppScreen(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val isRefreshing = uiState.screenState is HomeScreenState.Loading

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onEvent(HomeUiEvent.ForceSync) },
            modifier = Modifier.fillMaxSize()
        ) {
            val successState = uiState.screenState as? HomeScreenState.Success
            StandardPageLayout(scaffoldPadding = innerPadding) {
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
                                successState != null -> {
                                    NextContestHeroCard(successState.nextDrawInfo)
                                }
                                uiState.screenState is HomeScreenState.Loading -> {
                                    LoadingCard()
                                }
                            }
                    }
                }

                successState?.lastDraw?.let { draw ->
                    item(key = "last_draw") {
                        AnimateOnEntry(
                            delayMillis = staggerDelay(2).toLong(),
                            animation = EntryAnimation.SlideUp
                        ) {
                            LastDrawCard(
                                draw = draw,
                                details = successState.details,
                                onCheckGame = { numbers ->
                                    onNavigateToChecker(numbers.toSet())
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                val stats = uiState.statistics
                if (stats != null) {
                    item(key = "stats_panel") {
                        AnimateOnEntry(
                            delayMillis = staggerDelay(2).toLong(),
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
                            delayMillis = staggerDelay(2).toLong(),
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
                        delayMillis = staggerDelay(3).toLong(),
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
