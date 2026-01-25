@file:OptIn(ExperimentalMaterial3Api::class)

package com.cebolao.lotofacil.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.ui.components.layout.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.stats.DistributionChartsCard
import com.cebolao.lotofacil.ui.components.layout.EntryAnimation
import com.cebolao.lotofacil.ui.components.game.LastDrawCard
import com.cebolao.lotofacil.ui.components.game.NextContestHeroCard
import com.cebolao.lotofacil.ui.components.layout.StandardPageLayout
import com.cebolao.lotofacil.ui.components.stats.StatisticsPanel
import com.cebolao.lotofacil.ui.components.game.WelcomeCard
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.ui.theme.staggerDelay
import com.cebolao.lotofacil.ui.components.common.LoadingCard
import com.cebolao.lotofacil.ui.components.common.StandardAttentionCard
import com.cebolao.lotofacil.data.repository.DatabaseLoadingState
import com.cebolao.lotofacil.data.repository.LoadingPhase
import com.cebolao.lotofacil.presentation.viewmodel.HomeScreenState
import com.cebolao.lotofacil.presentation.viewmodel.HomeUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.HomeUiState
import com.cebolao.lotofacil.presentation.viewmodel.HomeViewModel
import com.cebolao.lotofacil.ui.theme.AppIcons

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
            navController?.navigate(com.cebolao.lotofacil.navigation.AppRoute.Results) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        listState = listState
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HomeUiEvent) -> Unit,
    onNavigateToChecker: (Set<Int>) -> Unit,
    onNavigateToResults: () -> Unit,
    listState: LazyListState
) {
    val isSyncing = uiState.isSyncing
    val isRefreshing = remember(isSyncing, uiState.screenState) {
        isSyncing || uiState.screenState is HomeScreenState.Loading
    }

    val successState = uiState.screenState as? HomeScreenState.Success
    val loadingStatus = remember(
        uiState.screenState,
        uiState.isSyncing,
        uiState.isStatsLoading,
        uiState.statistics,
        successState
    ) {
        when {
            uiState.screenState is HomeScreenState.Loading -> HomeLoadingStatus(
                titleRes = R.string.home_loading_initial_title,
                messageRes = R.string.home_loading_initial_message,
                progress = 0.25f
            )
            uiState.isSyncing -> HomeLoadingStatus(
                titleRes = R.string.home_loading_sync_title,
                messageRes = R.string.home_loading_sync_message,
                progress = 0.6f
            )
            uiState.isStatsLoading && successState != null && uiState.statistics == null -> HomeLoadingStatus(
                titleRes = R.string.home_loading_stats_title,
                messageRes = R.string.home_loading_stats_message,
                progress = 0.85f
            )
            else -> null
        }
    }
    val heroState = remember(uiState.screenState, successState) {
        when {
            successState?.nextDrawInfo != null -> HomeHeroState.Data(successState.nextDrawInfo)
            uiState.screenState is HomeScreenState.Loading -> HomeHeroState.Loading
            else -> HomeHeroState.Empty
        }
    }

    AppScreen(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(
                onClick = { onEvent(HomeUiEvent.ForceSync) },
                enabled = !isRefreshing
            ) {
                AnimatedContent(
                    targetState = isSyncing,
                    transitionSpec = {
                        fadeIn(animationSpec = Motion.Tween.fast()) togetherWith
                            fadeOut(animationSpec = Motion.Tween.fast())
                    },
                    label = "home_sync_action"
                ) { refreshing ->
                    if (refreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.IconSmall),
                            strokeWidth = Dimen.Border.Thin,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = AppIcons.Refresh,
                            contentDescription = stringResource(R.string.home_sync_button_description)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onEvent(HomeUiEvent.ForceSync) },
            modifier = Modifier.fillMaxSize()
        ) {
            StandardPageLayout(
                scaffoldPadding = innerPadding,
                listState = listState
            ) {
                item(key = "welcome") {
                    AnimateOnEntry(
                        delayMillis = staggerDelay(0).toLong(),
                        animation = EntryAnimation.Fade
                    ) {
                        WelcomeCard(nextDrawInfo = successState?.nextDrawInfo)
                    }
                }

                item(key = "loading_state") {
                    AnimatedContent(
                        targetState = loadingStatus,
                        transitionSpec = {
                            fadeIn(animationSpec = Motion.Tween.medium()) togetherWith
                                fadeOut(animationSpec = Motion.Tween.fast())
                        },
                        label = "home_loading_state"
                    ) { status ->
                        if (status == null) {
                            Spacer(Modifier.height(Dimen.SpacingTiny))
                        } else {
                            HomeLoadingCard(
                                status = status,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item(key = "hero") {
                    AnimateOnEntry(
                        delayMillis = staggerDelay(1).toLong(),
                        animation = EntryAnimation.Scale
                    ) {
                        AnimatedContent(
                            targetState = heroState,
                            transitionSpec = {
                                fadeIn(animationSpec = Motion.Tween.medium()) togetherWith
                                    fadeOut(animationSpec = Motion.Tween.fast())
                            },
                            label = "home_hero_state"
                        ) { state ->
                            when (state) {
                                HomeHeroState.Loading -> LoadingCard()
                                HomeHeroState.Empty -> Spacer(Modifier.height(Dimen.SpacingTiny))
                                is HomeHeroState.Data -> NextContestHeroCard(state.info)
                            }
                        }
                    }
                }

                (uiState.screenState as? HomeScreenState.Error)?.takeIf { !uiState.isSyncing }?.let { error ->
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

private data class HomeLoadingStatus(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    val progress: Float
)

private sealed interface HomeHeroState {
    data object Loading : HomeHeroState
    data object Empty : HomeHeroState
    data class Data(val info: NextDrawInfo) : HomeHeroState
}

@Composable
private fun HomeLoadingCard(
    status: HomeLoadingStatus,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val progress by animateFloatAsState(
        targetValue = status.progress,
        animationSpec = Motion.Tween.medium(),
        label = "home_loading_progress"
    )

    AppCard(
        modifier = modifier,
        outlined = true,
        contentPadding = Dimen.Spacing16
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
            ) {
                Icon(
                    imageVector = AppIcons.Refresh,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(Dimen.IconSmall)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    AnimatedContent(
                        targetState = status.titleRes,
                        transitionSpec = {
                            fadeIn(animationSpec = Motion.Tween.medium()) togetherWith
                                fadeOut(animationSpec = Motion.Tween.fast())
                        },
                        label = "home_loading_title"
                    ) { titleRes ->
                        Text(
                            text = stringResource(titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface
                        )
                    }
                    AnimatedContent(
                        targetState = status.messageRes,
                        transitionSpec = {
                            fadeIn(animationSpec = Motion.Tween.medium()) togetherWith
                                fadeOut(animationSpec = Motion.Tween.fast())
                        },
                        label = "home_loading_message"
                    ) { messageRes ->
                        Text(
                            text = stringResource(messageRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = progress,
                color = scheme.primary,
                trackColor = scheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
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
        verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing) // Reduzido de Medium para 8dp
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

@Composable
fun DatabaseLoadingCard(
    loadingState: DatabaseLoadingState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    when (loadingState) {
        is DatabaseLoadingState.Idle -> {
            Spacer(modifier = modifier)
        }
        is DatabaseLoadingState.Loading -> {
            val phaseTitle = when (loadingState.phase) {
                LoadingPhase.CHECKING -> R.string.db_loading_checking
                LoadingPhase.READING_ASSETS -> R.string.db_loading_reading_assets
                LoadingPhase.PARSING_DATA -> R.string.db_loading_parsing
                LoadingPhase.SAVING_TO_DATABASE -> R.string.db_loading_saving
                LoadingPhase.FINALIZING -> R.string.db_loading_finalizing
            }

            val progress by animateFloatAsState(
                targetValue = loadingState.progress,
                animationSpec = Motion.Tween.medium(),
                label = "db_loading_progress"
            )

            AppCard(
                modifier = modifier,
                outlined = true,
                contentPadding = Dimen.Spacing16
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimen.IconSmall),
                            strokeWidth = Dimen.Border.Thin,
                            color = scheme.primary
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                        ) {
                            Text(
                                text = stringResource(phaseTitle),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                            if (loadingState.totalCount > 0) {
                                Text(
                                    text = stringResource(
                                        R.string.db_loading_progress,
                                        loadingState.loadedCount,
                                        loadingState.totalCount
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        color = scheme.primary,
                        trackColor = scheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        is DatabaseLoadingState.Completed -> {
            AppCard(
                modifier = modifier,
                outlined = true,
                contentPadding = Dimen.Spacing16
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(Dimen.IconSmall)
                    )
                    Text(
                        text = stringResource(R.string.db_loading_complete, loadingState.loadedCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface
                    )
                }
            }
        }
        is DatabaseLoadingState.Failed -> {
            AppCard(
                modifier = modifier,
                outlined = true,
                contentPadding = Dimen.Spacing16
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                    ) {
                        Icon(
                            imageVector = AppIcons.Error,
                            contentDescription = null,
                            tint = scheme.error,
                            modifier = Modifier.size(Dimen.IconSmall)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                        ) {
                            Text(
                                text = stringResource(R.string.general_error_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = loadingState.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(
                        onClick = onRetry,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.db_loading_retry))
                    }
                }
            }
        }
    }
}
