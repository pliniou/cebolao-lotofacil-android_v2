package com.cebolao.lotofacil.presentation.viewmodel

import androidx.annotation.StringRes
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
import com.cebolao.lotofacil.presentation.util.UiEvent
import com.cebolao.lotofacil.presentation.util.UiState
import com.cebolao.lotofacil.ui.model.UiGameAnalysisResult
import com.cebolao.lotofacil.ui.model.UiLotofacilGame
import com.cebolao.lotofacil.ui.model.toDomain
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.util.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.ensureActive
import java.math.BigDecimal
import javax.inject.Inject
import com.cebolao.lotofacil.mapper.toUiModel as toUiAnalysisResult
import com.cebolao.lotofacil.ui.model.toUiModel as toUiModelGame

/**
 * Summary of all games (pinned and unpinned).
 */
@androidx.compose.runtime.Immutable
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
    val analysisState: GameAnalysisUiState = GameAnalysisUiState.Idle,
    val isLoading: Boolean = false,
    val unpinnedGames: List<UiLotofacilGame> = emptyList(),
    val pinnedGames: List<UiLotofacilGame> = emptyList(),
    @param:StringRes val errorMessageRes: Int? = null
) : UiState

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
    private val _uiError = MutableStateFlow<Int?>(null)
    private val _isLoading = MutableStateFlow(true)

    private var analyzeJob: Job? = null

    private val _gamesState = combine(
        gameRepository.unpinnedGames,
        gameRepository.pinnedGames
    ) { unpinned, pinned ->
        Pair(
            unpinned.map { it.toUiModelGame() },
            pinned.map { it.toUiModelGame() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), Pair(emptyList(), emptyList()))

    val uiState: StateFlow<GameScreenUiState> = combine(
        _gameToDelete,
        _analysisState,
        _gamesState,
        _isLoading,
        _uiError
    ) { gameToDelete, analysisState, (unpinned, pinned), isLoading, errorMessageRes ->
        val totalGamesCount = unpinned.size + pinned.size
        val pinnedCount = pinned.size
        val totalCost = GameConstants.GAME_COST.multiply(BigDecimal(totalGamesCount))
        
        GameScreenUiState(
            gameToDelete = gameToDelete,
            summary = GameSummary(
                totalGames = totalGamesCount,
                pinnedGames = pinnedCount,
                totalCost = totalCost
            ),
            analysisState = analysisState,
            isLoading = isLoading,
            unpinnedGames = unpinned,
            pinnedGames = pinned,
            errorMessageRes = errorMessageRes
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        GameScreenUiState(isLoading = true)
    )

    init {
        // First load finishes loading state
        viewModelScope.launch {
            _gamesState.collect {
                _isLoading.value = false
                _uiError.value = null
            }
        }
    }

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
        _uiError.value = null
        viewModelScope.launchCatching(
            onError = {
                _events.trySend(GameEffect.ShowSnackbar(R.string.error_toggle_pin_failed))
                _uiError.value = R.string.error_toggle_pin_failed
            }
        ) {
            when (val result = toggleGamePinUseCase(game.toDomain())) {
                is AppResult.Success -> {
                    val msg = if (game.isPinned) {
                        R.string.games_unpin_success
                    } else {
                        R.string.games_pin_success
                    }
                    _events.trySend(GameEffect.ShowSnackbar(msg))
                }
                is AppResult.Failure -> {
                    _events.trySend(GameEffect.ShowSnackbar(R.string.error_toggle_pin_failed))
                    _uiError.value = R.string.error_toggle_pin_failed
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
        _uiError.value = null
        viewModelScope.launchCatching(
            onError = {
                _events.trySend(GameEffect.ShowSnackbar(R.string.error_delete_game_failed))
                _uiError.value = R.string.error_delete_game_failed
            }
        ) {
            try {
                when (gameRepository.deleteGame(game.toDomain())) {
                    is AppResult.Success -> {
                        _events.trySend(GameEffect.ShowSnackbar(R.string.games_delete_success))
                    }
                    is AppResult.Failure -> {
                        _events.trySend(GameEffect.ShowSnackbar(R.string.error_delete_game_failed))
                        _uiError.value = R.string.error_delete_game_failed
                    }
                }
            } finally {
                _gameToDelete.value = null
            }
        }
    }

    private fun clearUnpinned() {
        _uiError.value = null
        viewModelScope.launchCatching(
            onError = {
                _events.trySend(GameEffect.ShowSnackbar(R.string.error_clear_games_failed))
                _uiError.value = R.string.error_clear_games_failed
            }
        ) {
            when (gameRepository.clearUnpinnedGames()) {
                is AppResult.Success -> _events.trySend(GameEffect.ShowSnackbar(R.string.games_clear_success))
                is AppResult.Failure -> {
                    _events.trySend(GameEffect.ShowSnackbar(R.string.error_clear_games_failed))
                    _uiError.value = R.string.error_clear_games_failed
                }
            }
        }
    }

    private fun analyzeGame(game: UiLotofacilGame) {
        if (_analysisState.value is GameAnalysisUiState.Loading) return

        analyzeJob?.cancel()
        _uiError.value = null
        analyzeJob = viewModelScope.launchCatching {
            _analysisState.value = GameAnalysisUiState.Loading
            val domainGame = game.toDomain()
            when (val result = analyzeGameUseCase(domainGame)) {
                is AppResult.Success -> {
                    _analysisState.value = GameAnalysisUiState.Success(result.value.toUiAnalysisResult())
                }
                is AppResult.Failure -> {
                    _analysisState.value = GameAnalysisUiState.Error(R.string.error_analysis_failed)
                    _uiError.value = R.string.error_analysis_failed
                }
            }
        }
    }

    private fun dismissAnalysis() {
        analyzeJob?.cancel()
        _analysisState.value = GameAnalysisUiState.Idle
    }

    private fun shareGame(game: UiLotofacilGame) {
        _uiError.value = null
        viewModelScope.launchCatching(
            onError = {
                _events.trySend(GameEffect.ShowSnackbar(R.string.error_share_game_failed))
                _uiError.value = R.string.error_share_game_failed
            }
        ) {
            val sortedNumbers = game.numbers.sorted()
            val lastDraw = when (val result = historyRepository.getLastDraw()) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> null
            }
            val metrics = metricsCalculator.calculate(game.toDomain(), lastDraw?.numbers)

            _events.trySend(GameEffect.ShareGame(numbers = sortedNumbers, metrics = metrics))
        }
    }

    override fun onCleared() {
        analyzeJob?.cancel()
        _events.close()
        super.onCleared()
    }
}

sealed interface GameUiEvent : UiEvent {
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
