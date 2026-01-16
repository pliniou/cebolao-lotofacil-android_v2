package com.cebolao.lotofacil.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.navigation.FiltersRoute
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.presentation.viewmodel.GameEffect
import com.cebolao.lotofacil.presentation.viewmodel.GameAnalysisUiState
import com.cebolao.lotofacil.presentation.viewmodel.GameScreenUiState
import com.cebolao.lotofacil.presentation.viewmodel.GameUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.GameViewModel
import com.cebolao.lotofacil.ui.components.common.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.common.MessageState
import com.cebolao.lotofacil.ui.components.game.GameCard
import com.cebolao.lotofacil.ui.components.game.GameCardAction
import com.cebolao.lotofacil.ui.components.layout.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.model.UiLotofacilGame
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.Formatters
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratedGamesScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val unpinned by viewModel.unpinnedGames.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedGames.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameEffect.ShareGame -> {
                    try {
                        val numbersFormatted = event.numbers.joinToString(" - ") { it.toString().padStart(2, '0') }
                        val text = currentContext.getString(
                            R.string.share_game_text_format,
                            numbersFormatted,
                            event.metrics.sum,
                            event.metrics.evens,
                            event.metrics.primes,
                            event.metrics.frame,
                            event.metrics.center,
                            event.metrics.fibonacci,
                            event.metrics.multiplesOf3,
                            event.metrics.repeated,
                            event.metrics.sequences
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val chooserIntent = Intent.createChooser(
                            intent,
                            currentContext.getString(R.string.games_share_chooser_title)
                        )
                        currentContext.startActivity(chooserIntent)
                    } catch (_: SecurityException) {
                        snackbarHostState.showSnackbar(currentContext.getString(R.string.share_error_message))
                    } catch (_: ActivityNotFoundException) {
                        snackbarHostState.showSnackbar(currentContext.getString(R.string.share_error_message))
                    }
                }
                is GameEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(currentContext.getString(event.messageRes))
                }
            }
        }
    }

    val onGenerateRequest = remember(navController) {
        {
            navController.navigate(FiltersRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    GeneratedGamesScreenContent(
        unpinned = unpinned,
        pinned = pinned,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onGenerateRequest = onGenerateRequest,
        onEvent = viewModel::onEvent,
        onNavigateToChecker = { numbers -> navController.navigateToChecker(numbers) }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratedGamesScreenContent(
    unpinned: List<UiLotofacilGame>,
    pinned: List<UiLotofacilGame>,
    uiState: GameScreenUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: (() -> Unit)?,
    onGenerateRequest: () -> Unit,
    onEvent: (GameUiEvent) -> Unit,
    onNavigateToChecker: (Set<Int>) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var gameToPin by remember { mutableStateOf<UiLotofacilGame?>(null) }

    if (showClearDialog) {
        AppConfirmationDialog(
            title = R.string.games_clear_dialog_title,
            message = R.string.games_clear_dialog_message,
            confirmText = R.string.games_clear_confirm,
            onConfirm = {
                onEvent(GameUiEvent.ClearUnpinned)
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false },
            icon = AppIcons.DeleteSweep
        )
    }

    if (uiState.gameToDelete != null) {
        AppConfirmationDialog(
            title = R.string.games_delete_dialog_title,
            message = R.string.games_delete_dialog_message,
            confirmText = R.string.games_delete_confirm,
            onConfirm = { onEvent(GameUiEvent.ConfirmDelete) },
            onDismiss = { onEvent(GameUiEvent.DismissDeleteDialog) },
            icon = AppIcons.Delete
        )
    }

    gameToPin?.let { game ->
        val isPinning = !game.isPinned
        AppConfirmationDialog(
            title = if (isPinning) R.string.game_card_pin_confirm_title else R.string.game_card_unpin_confirm_title,
            message = if (isPinning) R.string.game_card_pin_confirm_msg else R.string.game_card_unpin_confirm_msg,
            confirmText = if (isPinning) R.string.general_save else R.string.general_close,
            onConfirm = { 
                onEvent(GameUiEvent.TogglePin(game))
                gameToPin = null
            },
            onDismiss = { gameToPin = null },
            icon = if (isPinning) AppIcons.PinFilled else AppIcons.PinOutlined
        )
    }

    if (uiState.analysisState !is GameAnalysisUiState.Idle) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onEvent(GameUiEvent.DismissAnalysis) }
        ) {
            GameAnalysisSheetContent(
                analysisState = uiState.analysisState,
                onDismiss = { onEvent(GameUiEvent.DismissAnalysis) }
            )
        }
    }

    val onAction = remember(onEvent, onNavigateToChecker) {
        { action: GameCardAction, game: UiLotofacilGame ->
            when (action) {
                GameCardAction.Analyze -> onEvent(GameUiEvent.AnalyzeGame(game))
                GameCardAction.Pin -> {
                    gameToPin = game
                }
                GameCardAction.Delete -> onEvent(GameUiEvent.RequestDelete(game))
                GameCardAction.Check -> onNavigateToChecker(game.numbers)
                GameCardAction.Share -> onEvent(GameUiEvent.ShareGame(game))
            }
        }
    }

    AppScreen(
        title = stringResource(R.string.games_title),
        subtitle = stringResource(R.string.games_subtitle),
        navigationIcon = if (onNavigateBack != null) {
            {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = AppIcons.ArrowBack,
                        contentDescription = stringResource(R.string.general_back)
                    )
                }
            }
        } else {
            {
                Icon(
                    imageVector = AppIcons.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            val isUnpinnedTab = pagerState.currentPage == 0
            if (isUnpinnedTab && unpinned.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = AppIcons.DeleteSweep,
                        contentDescription = stringResource(R.string.games_clear_unpinned_button_description)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GameTabs(pagerState.currentPage) { index ->
                scope.launch { pagerState.animateScrollToPage(index) }
            }

            GameSummaryCard(
                summary = uiState.summary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimen.ScreenPadding,
                        vertical = Dimen.ItemSpacing
                    )
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val games = if (page == 0) unpinned else pinned
                val isNewGamesTab = page == 0

                GameList(
                    games = games,
                    isNewGamesTab = isNewGamesTab,
                    onGenerateRequest = onGenerateRequest,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    isNewGamesTab: Boolean,
    onGenerateRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimen.ScreenPadding,
                vertical = Dimen.SectionSpacing
            ),
        contentAlignment = Alignment.Center
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            outlined = false,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            MessageState(
                icon = AppIcons.List,
                title = stringResource(R.string.games_empty_state_title),
                message = stringResource(
                    if (isNewGamesTab) R.string.games_empty_state_description
                    else R.string.widget_no_pinned_games
                ),
                actionLabel = if (isNewGamesTab) stringResource(R.string.filters_button_generate) else null,
                onActionClick = if (isNewGamesTab) onGenerateRequest else null,
                modifier = Modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        divider = {}
    ) {
        val tabs = listOf(R.string.games_tab_new, R.string.games_tab_pinned)
        tabs.forEachIndexed { index, titleRes ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun GameList(
    games: List<UiLotofacilGame>,
    isNewGamesTab: Boolean,
    onGenerateRequest: () -> Unit,
    onAction: (GameCardAction, UiLotofacilGame) -> Unit
) {
    val gridState = rememberLazyGridState()
    val animateCards by remember(games.size) { derivedStateOf { games.size < 40 } }

    if (games.isEmpty()) {
        EmptyState(isNewGamesTab, onGenerateRequest)
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(
                top = Dimen.ItemSpacing,
                bottom = Dimen.SectionSpacing,
                start = Dimen.ScreenPadding,
                end = Dimen.ScreenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing),
            horizontalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            itemsIndexed(
                items = games,
                key = { _, game -> game.mask },
                contentType = { _, _ -> "game_card" }
            ) { index, game ->
                val card: @Composable () -> Unit = {
                    GameCard(
                        game = game,
                        index = index + 1,
                        modifier = Modifier.aspectRatio(1f),
                        onAction = { action -> onAction(action, game) }
                    )
                }
                if (animateCards) {
                    AnimateOnEntry(delayMillis = (index * 40).toLong()) { card() }
                } else {
                    card()
                }
            }
        }
    }
}

@Composable
private fun GameSummaryCard(
    summary: com.cebolao.lotofacil.presentation.viewmodel.GameSummary,
    modifier: Modifier = Modifier
) {
    if (summary.totalGames <= 0) return

    AppCard(
        modifier = modifier,
        outlined = true,
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

@Composable
private fun GameAnalysisSheetContent(
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
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = AppIcons.CloseOutlined,
                    contentDescription = stringResource(R.string.general_close)
                )
            }
        }

        when (analysisState) {
            is GameAnalysisUiState.Idle -> Unit
            is GameAnalysisUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimen.SectionSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }
            is GameAnalysisUiState.Error -> {
                Text(
                    text = stringResource(analysisState.messageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is GameAnalysisUiState.Success -> {
                Text(
                    text = analysisState.result.game.numbers.sorted().joinToString(" - ") { it.toString().padStart(2, '0') },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                com.cebolao.lotofacil.ui.components.stats.FinancialPerformanceCard(
                    report = analysisState.result.checkReport,
                    modifier = Modifier.fillMaxWidth()
                )

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    outlined = true,
                    title = stringResource(R.string.games_analysis_stats_title)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)) {
                        analysisState.result.simpleStats.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = k,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = v,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
