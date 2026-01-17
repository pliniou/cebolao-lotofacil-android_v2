package com.cebolao.lotofacil.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameMetricsCalculator
import com.cebolao.lotofacil.domain.usecase.AnalyzeGameUseCase
import com.cebolao.lotofacil.domain.usecase.ToggleGamePinUseCase
import com.cebolao.lotofacil.ui.model.UiGameAnalysisResult
import com.cebolao.lotofacil.ui.model.UiLotofacilGame
import com.cebolao.lotofacil.ui.model.toDomain
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject
import com.cebolao.lotofacil.mapper.toUiModel as toUiAnalysisResult
import com.cebolao.lotofacil.ui.model.toUiModel as toUiModelGame

/**
 * Summary of all games (pinned and unpinned).
 */
data class GameSummary(
    val totalGames: Int = 0,
    val pinnedGames: Int = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO
)

/**
 * UI state for the Game screen.
 */
@androidx.compose.runtime.Immutable
data class GameScreenUiState(
    val gameToDelete: UiLotofacilGame? = null,
    val summary: GameSummary = GameSummary(),
    val analysisState: GameAnalysisUiState = GameAnalysisUiState.Idle
)

/**
 * Represents the state of a game analysis operation.
 */
sealed interface GameAnalysisUiState {
    /** No analysis in progress. */
    data object Idle : GameAnalysisUiState

    /** Analysis is currently running. */
    data object Loading : GameAnalysisUiState

    /** Analysis completed successfully. */
    data class Success(val result: UiGameAnalysisResult) : GameAnalysisUiState

    /** Analysis failed with an error. */
    data class Error(@param:StringRes val messageResId: Int) : GameAnalysisUiState
}

/**
 * ViewModel for the Game screen.
 * Manages user's lottery games, including pinning, deletion, and analysis.
 */
@HiltViewModel
    val analysisState: GameAnalysisUiState = GameAnalysisUiState.Idle
)

/**
 * Represents the state of a game analysis operation.
 */
sealed interface GameAnalysisUiState {
    /** No analysis in progress. */
    data object Idle : GameAnalysisUiState

    /** Analysis is currently running. */
    data object Loading : GameAnalysisUiState

    /** Analysis completed successfully. */
    data class Success(val result: UiGameAnalysisResult) : GameAnalysisUiState

    /** Analysis failed with an error. */
    data class Error(@param:StringRes val messageResId: Int) : GameAnalysisUiState
}

/**
 * ViewModel for the Game screen.
 * Manages user's lottery games, including pinning, deletion, and analysis.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val analyzeGameUseCase: AnalyzeGameUseCase,
    private val toggleGamePinUseCase: ToggleGamePinUseCase,
    private val historyRepository: HistoryRepository,
    private val metricsCalculator: GameMetricsCalculator
) : BaseViewModel() {

    private val _events = Channel<GameEffect>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _gameToDelete = MutableStateFlow<UiLotofacilGame?>(null)
    private val _analysisState = MutableStateFlow<GameAnalysisUiState>(GameAnalysisUiState.Idle)

    private var analyzeJob: Job? = null

    private val gamesFlow: StateFlow<List<UiLotofacilGame>> = combine(
        gameRepository.unpinnedGames,
        gameRepository.pinnedGames
    ) { unpinned, pinned -> unpinned + pinned }
        .map { games: List<LotofacilGame> -> games.map { game: LotofacilGame -> game.toUiModelGame() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), emptyList())

    val unpinnedGames = gamesFlow.map { games ->
        games.filter { !it.isPinned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), emptyList())

    val pinnedGames = gamesFlow.map { games ->
        games.filter { it.isPinned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), emptyList())

    val uiState: StateFlow<GameScreenUiState> = combine(
        _gameToDelete,
        _analysisState,
        gamesFlow
    ) { gameToDelete, analysisState, games ->
        val pinnedCount = games.count { it.isPinned }
        val totalCost = GameConstants.GAME_COST.multiply(BigDecimal(games.size))
        
        GameScreenUiState(
            gameToDelete = gameToDelete,
            summary = GameSummary(
                totalGames = games.size,
                pinnedGames = pinnedCount,
                totalCost = totalCost
            ),
            analysisState = analysisState
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        GameScreenUiState()
    )

    fun onEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.TogglePin -> togglePinState(event.game)
            is GameUiEvent.RequestDelete -> requestDeleteGame(event.game)
            is GameUiEvent.ConfirmDelete -> confirmDeleteGame()
            is GameUiEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is GameUiEvent.ClearUnpinned -> clearUnpinned()
            is GameUiEvent.AnalyzeGame -> analyzeGame(event.game)
            is GameUiEvent.ShareGame -> shareGame(event.game)
            is GameUiEvent.DismissAnalysis -> dismissAnalysis()
        }
    }

    private fun togglePinState(game: UiLotofacilGame) {
        launchCatching(
            onError = { _events.trySend(GameEffect.ShowSnackbar(R.string.error_toggle_pin_failed)) }
        ) {
            when (val result = toggleGamePinUseCase(game.toDomain())) {
                is AppResult.Success -> {
                    val msg = if (game.isPinned) {
                        R.string.game_card_unpinned
                    } else {
                        R.string.game_card_pinned
                    }
                    _events.send(GameEffect.ShowSnackbar(msg))
                }
                is AppResult.Failure -> {
                    _events.send(GameEffect.ShowSnackbar(R.string.error_toggle_pin_failed))
                }
            }
        }
    }

    private fun requestDeleteGame(game: UiLotofacilGame) {
        _gameToDelete.value = game
    }

    private fun dismissDeleteDialog() {
        _gameToDelete.value = null
    }

    private fun confirmDeleteGame() {
        val game = _gameToDelete.value ?: return
        launchCatching(
            onError = { _events.trySend(GameEffect.ShowSnackbar(R.string.error_delete_game_failed)) }
        ) {
            try {
                gameRepository.deleteGame(game.toDomain())
            } finally {
                _gameToDelete.value = null
            }
        }
    }

    private fun clearUnpinned() {
        launchCatching(
            onError = { _events.trySend(GameEffect.ShowSnackbar(R.string.error_clear_games_failed)) }
        ) {
            gameRepository.clearUnpinnedGames()
        }
    }

    private fun analyzeGame(game: UiLotofacilGame) {
        if (_analysisState.value is GameAnalysisUiState.Loading) return

        analyzeJob?.cancel()
        analyzeJob = launchCatching {
            _analysisState.value = GameAnalysisUiState.Loading
            val domainGame = game.toDomain()
            when (val result = analyzeGameUseCase(domainGame)) {
                is AppResult.Success -> {
                    _analysisState.value = GameAnalysisUiState.Success(result.value.toUiAnalysisResult())
                }
                is AppResult.Failure -> {
                    _analysisState.value = GameAnalysisUiState.Error(R.string.error_analysis_failed)
                }
            }
        }
    }

    private fun dismissAnalysis() {
        analyzeJob?.cancel()
        _analysisState.value = GameAnalysisUiState.Idle
    }

    private fun shareGame(game: UiLotofacilGame) {
        launchCatching(
            onError = { _events.trySend(GameEffect.ShowSnackbar(R.string.error_share_game_failed)) }
        ) {
            val sortedNumbers = game.numbers.sorted()
            val lastDraw = historyRepository.getLastDraw()
            val metrics = metricsCalculator.calculate(game.toDomain(), lastDraw?.numbers)

            _events.send(GameEffect.ShareGame(numbers = sortedNumbers, metrics = metrics))
        }
    }

    override fun onCleared() {
        analyzeJob?.cancel()
        _events.close()
        super.onCleared()
    }
}

sealed interface GameUiEvent {
    data class TogglePin(val game: UiLotofacilGame) : GameUiEvent
    data class RequestDelete(val game: UiLotofacilGame) : GameUiEvent
    data object ConfirmDelete : GameUiEvent
    data object DismissDeleteDialog : GameUiEvent
    data object ClearUnpinned : GameUiEvent
    data class AnalyzeGame(val game: UiLotofacilGame) : GameUiEvent
    data class ShareGame(val game: UiLotofacilGame) : GameUiEvent
    data object DismissAnalysis : GameUiEvent
}

/**
 * Effects emitted by the Game screen.
 * (Previously GameScreenEvent)
 */
sealed interface GameEffect {
    data class ShareGame(
        val numbers: List<Int>,
        val metrics: GameComputedMetrics
    ) : GameEffect

    data class ShowSnackbar(@param:StringRes val messageRes: Int) : GameEffect
}
