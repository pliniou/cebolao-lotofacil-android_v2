package com.cebolao.lotofacil.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.GameCard
import com.cebolao.lotofacil.ui.components.GameCardAction
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.GameViewModel
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
    val pagerState = rememberPagerState(pageCount = { 2 })

    var showClearDialog by remember { mutableStateOf(false) }
    var gameToPin by remember { mutableStateOf<LotofacilGame?>(null) }

    if (showClearDialog) {
        AppConfirmationDialog(
            title = R.string.games_clear_dialog_title,
            message = R.string.games_clear_dialog_message,
            confirmText = R.string.games_clear_confirm,
            onConfirm = {
                viewModel.clearUnpinned()
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
            onConfirm = viewModel::confirmDeleteGame,
            onDismiss = viewModel::dismissDeleteDialog,
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
                viewModel.togglePinState(game)
                gameToPin = null
            },
            onDismiss = { gameToPin = null },
            icon = if (isPinning) AppIcons.PinFilled else AppIcons.PinOutlined
        )
    }

    // Consumir eventos do ViewModel (share e snackbar)
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.cebolao.lotofacil.viewmodels.GameScreenEvent.ShareGame -> {
                    try {
                        val numbersFormatted = event.numbers.joinToString(" - ") { it.toString().padStart(2, '0') }
                        val text = context.getString(
                            R.string.share_game_text_format,
                            numbersFormatted,
                            event.sum,
                            event.evens
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(
                                intent,
                                context.getString(R.string.games_share_chooser_title)
                            )
                        )
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.share_error_message))
                        }
                    }
                }
                is com.cebolao.lotofacil.viewmodels.GameScreenEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(event.messageRes))
                    }
                }
            }
        }
    }

    val onGenerateRequest = remember(navController) {
        {
            navController.navigate(Screen.Filters.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val onAction = remember(viewModel, navController) {
        { action: GameCardAction, game: LotofacilGame ->
            when (action) {
                GameCardAction.Analyze -> viewModel.analyzeGame(game)
                GameCardAction.Pin -> {
                    gameToPin = game
                }
                GameCardAction.Delete -> viewModel.requestDeleteGame(game)
                GameCardAction.Check -> navController.navigateToChecker(game.numbers)
                GameCardAction.Share -> viewModel.shareGame(game)
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
                    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
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
        com.cebolao.lotofacil.ui.components.AppCard(
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
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun GameList(
    games: List<LotofacilGame>,
    isNewGamesTab: Boolean,
    onGenerateRequest: () -> Unit,
    onAction: (GameCardAction, LotofacilGame) -> Unit
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
                top = Dimen.Spacing16,
                bottom = Dimen.Spacing24,
                start = Dimen.ScreenPadding,
                end = Dimen.ScreenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16),
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
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
