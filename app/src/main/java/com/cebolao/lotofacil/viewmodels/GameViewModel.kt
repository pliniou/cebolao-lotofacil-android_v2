package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.usecase.AnalyzeGameUseCase
import com.cebolao.lotofacil.domain.usecase.ClearUnpinnedGamesUseCase
import com.cebolao.lotofacil.domain.usecase.DeleteGameUseCase
import com.cebolao.lotofacil.domain.usecase.ObservePinnedGamesUseCase
import com.cebolao.lotofacil.domain.usecase.ObserveUnpinnedGamesUseCase
import com.cebolao.lotofacil.domain.usecase.TogglePinStateUseCase
import com.cebolao.lotofacil.mapper.toUiModel
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

sealed interface GameScreenEvent {
    data class ShareGame(val numbers: List<Int>, val sum: Int, val evens: Int) : GameScreenEvent
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : GameScreenEvent
}

data class GameSummary(
    val totalGames: Int = 0,
    val pinnedGames: Int = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO
)

data class GameScreenUiState(
    val gameToDelete: LotofacilGame? = null,
    val summary: GameSummary = GameSummary(),
    val analysisState: GameAnalysisUiState = GameAnalysisUiState.Idle
)

data class GameAnalysisResult(
    val game: LotofacilGame,
    val simpleStats: ImmutableList<Pair<String, String>>,
    val checkReport: CheckReport
)

sealed interface GameAnalysisUiState {
    data object Idle : GameAnalysisUiState
    data object Loading : GameAnalysisUiState
    data class Success(val result: GameAnalysisResult) : GameAnalysisUiState
    data class Error(@param:StringRes val messageResId: Int) : GameAnalysisUiState
}

@HiltViewModel
class GameViewModel @Inject constructor(
    observeUnpinnedGamesUseCase: ObserveUnpinnedGamesUseCase,
    observePinnedGamesUseCase: ObservePinnedGamesUseCase,
    private val togglePinStateUseCase: TogglePinStateUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val clearUnpinnedGamesUseCase: ClearUnpinnedGamesUseCase,
    private val analyzeGameUseCase: AnalyzeGameUseCase
) : ViewModel() {

    val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>> = observeUnpinnedGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = observePinnedGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    private val _gameToDelete = MutableStateFlow<LotofacilGame?>(null)
    private val _analysisState = MutableStateFlow<GameAnalysisUiState>(GameAnalysisUiState.Idle)

    private val _events = Channel<GameScreenEvent>(Channel.BUFFERED)

    @Suppress("unused") // coletado pela UI quando conectado (LaunchedEffect)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<GameScreenUiState> = combine(
        unpinnedGames,
        pinnedGames,
        _gameToDelete,
        _analysisState
    ) { unpinned, pinned, gameToDelete, analysis ->
        val total = unpinned.size + pinned.size
        GameScreenUiState(
            gameToDelete = gameToDelete,
            analysisState = analysis,
            summary = GameSummary(
                totalGames = total,
                pinnedGames = pinned.size,
                totalCost = GameConstants.GAME_COST.multiply(BigDecimal(total))
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        GameScreenUiState()
    )

    private var analyzeJob: Job? = null

    fun togglePinState(game: LotofacilGame) {
        viewModelScope.launch {
            try {
                togglePinStateUseCase(game)
                val msg = if (game.isPinned) R.string.game_card_unpinned else R.string.game_card_pinned
                _events.send(GameScreenEvent.ShowSnackbar(msg))
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) {
                _events.send(GameScreenEvent.ShowSnackbar(R.string.error_analysis_failed))
            }
        }
    }

    fun requestDeleteGame(game: LotofacilGame) {
        _gameToDelete.value = game
    }

    fun dismissDeleteDialog() {
        _gameToDelete.value = null
    }

    fun confirmDeleteGame() {
        val game = _gameToDelete.value ?: return
        viewModelScope.launch {
            try {
                deleteGameUseCase(game)
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) {
                _events.send(GameScreenEvent.ShowSnackbar(R.string.error_analysis_failed))
            } finally {
                _gameToDelete.value = null
            }
        }
    }

    fun clearUnpinned() {
        viewModelScope.launch {
            try {
                clearUnpinnedGamesUseCase()
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) {
                _events.send(GameScreenEvent.ShowSnackbar(R.string.error_analysis_failed))
            }
        }
    }

    fun analyzeGame(game: LotofacilGame) {
        if (_analysisState.value is GameAnalysisUiState.Loading) return

        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _analysisState.value = GameAnalysisUiState.Loading
            analyzeGameUseCase(game)
                .onSuccess { _analysisState.value = GameAnalysisUiState.Success(it.toUiModel()) }
                .onFailure { _analysisState.value = GameAnalysisUiState.Error(R.string.error_analysis_failed) }
        }
    }

    @Suppress("unused")
    fun dismissAnalysis() {
        _analysisState.value = GameAnalysisUiState.Idle
    }

    fun shareGame(game: LotofacilGame) {
        viewModelScope.launch {
            val sortedNumbers = game.numbers.sorted()
            val sum = sortedNumbers.sum()
            val evens = sortedNumbers.count { it % 2 == 0 }
            _events.send(GameScreenEvent.ShareGame(numbers = sortedNumbers, sum = sum, evens = evens))
        }
    }

    override fun onCleared() {
        analyzeJob?.cancel()
        _events.close()
        super.onCleared()
    }
}
