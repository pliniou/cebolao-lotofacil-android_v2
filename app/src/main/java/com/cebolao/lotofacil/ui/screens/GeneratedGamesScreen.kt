package com.cebolao.lotofacil.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    val scope = rememberCoroutineScope()
    
    // Consumir eventos do ViewModel (share e snackbar)
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentContext by rememberUpdatedState(context)
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
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
                        }
                        val chooserIntent = Intent.createChooser(
                            intent,
                            currentContext.getString(R.string.games_share_chooser_title)
                        )
                        currentContext.startActivity(chooserIntent)
                    } catch (_: SecurityException) {
                        scope.launch {
                            snackbarHostState.showSnackbar(currentContext.getString(R.string.share_error_message))
                        }
                    } catch (_: ActivityNotFoundException) {
                        scope.launch {
                            snackbarHostState.showSnackbar(currentContext.getString(R.string.share_error_message))
                        }
                    }
                }
                is GameEffect.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(currentContext.getString(event.messageRes))
                    }
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
    var gameToPin by rememberSaveable { mutableStateOf<UiLotofacilGame?>(null) }

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
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            GameTabs(pagerState.currentPage) { index ->
                scope.launch { pagerState.animateScrollToPage(index) }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val games = if (page == 0) unpinned else pinned
                val isNewGamesTab = page == 0

                if (isNewGamesTab) {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onGenerateRequest,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        GameList(
                            games = games,
                            isNewGamesTab = true,
                            onGenerateRequest = onGenerateRequest,
                            onAction = onAction
                        )
                    }
                } else {
                    GameList(
                        games = games,
                        isNewGamesTab = false,
                        onGenerateRequest = onGenerateRequest,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    isNewGamesTab: Boolean,
    onGenerateRequest: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimen.ScreenPadding,
                vertical = Dimen.SectionSpacing
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
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
        containerColor = MaterialTheme.colorScheme.background,
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
    if (games.isEmpty()) {
        EmptyState(isNewGamesTab, onGenerateRequest)
        return
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxSize(),
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
                key = { _, game -> game.numbers.sorted().joinToString(separator = "-") },
                contentType = { _, _ -> "game_card" }
            ) { index, game ->
                AnimateOnEntry {
                    GameCard(
                        game = game,
                        index = index + 1,
                        onAction = { action -> onAction(action, game) }
                    )
                }
            }
        }
    }
}
