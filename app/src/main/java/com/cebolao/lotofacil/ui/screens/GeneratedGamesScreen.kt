package com.cebolao.lotofacil.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.navigation.AppRoute
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.presentation.viewmodel.GameAnalysisUiState
import com.cebolao.lotofacil.presentation.viewmodel.GameEffect
import com.cebolao.lotofacil.presentation.viewmodel.GameScreenUiState
import com.cebolao.lotofacil.presentation.viewmodel.GameUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.GameViewModel
import com.cebolao.lotofacil.ui.components.common.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.common.LoadingCard
import com.cebolao.lotofacil.ui.components.common.StandardAttentionCard
import com.cebolao.lotofacil.ui.components.game.GameAnalysisSheetContent
import com.cebolao.lotofacil.ui.components.game.GameCardAction
import com.cebolao.lotofacil.ui.components.game.GameList
import com.cebolao.lotofacil.ui.components.game.GameSummaryCard
import com.cebolao.lotofacil.ui.components.game.GameTabs
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
            navController.navigate(AppRoute.Filters) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    GeneratedGamesScreenContent(
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
                    contentDescription = stringResource(R.string.games_title),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            val isUnpinnedTab = pagerState.currentPage == 0
            if (isUnpinnedTab && uiState.unpinnedGames.isNotEmpty()) {
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
                val games by remember(uiState.unpinnedGames, uiState.pinnedGames, page) {
                    derivedStateOf {
                        if (page == 0) uiState.unpinnedGames else uiState.pinnedGames
                    }
                }
                val isNewGamesTab = page == 0

                when {
                    uiState.isLoading -> {
                        LoadingCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimen.ScreenPadding,
                                    vertical = Dimen.SectionSpacing
                                ),
                            title = stringResource(R.string.general_loading),
                            description = stringResource(R.string.loading_details_fallback)
                        )
                    }
                    uiState.errorMessageRes != null -> {
                        StandardAttentionCard(
                            title = stringResource(R.string.general_error_title),
                            message = stringResource(uiState.errorMessageRes),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimen.ScreenPadding,
                                    vertical = Dimen.SectionSpacing
                                ),
                            icon = AppIcons.Error
                        )
                    }
                    else -> {
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
    }
}
