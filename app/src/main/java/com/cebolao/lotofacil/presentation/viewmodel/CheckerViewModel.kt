package com.cebolao.lotofacil.presentation.viewmodel


import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.navigation.toRoute
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.GameScore
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.MaskUtils
import com.cebolao.lotofacil.domain.repository.CheckRunRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameMetricsCalculator
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.navigation.CheckerRoute
import com.cebolao.lotofacil.util.toUserMessageRes
import com.cebolao.lotofacil.util.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CheckerViewModel @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val metricsCalculator: GameMetricsCalculator,
    private val historyRepository: HistoryRepository,
    private val checkRunRepository: CheckRunRepository,
    private val logger: Logger,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val externalScope: CoroutineScope,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    companion object {
        private const val TAG = "CheckerViewModel"
    }

    private val _uiState = MutableStateFlow<CheckerUiState>(CheckerUiState.Idle)
    val uiState: StateFlow<CheckerUiState> = _uiState.asStateFlow()

    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers: StateFlow<Set<Int>> = _selectedNumbers.asStateFlow()

    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()

    private val _gameScore = MutableStateFlow<GameScore?>(null)
    val gameScore: StateFlow<GameScore?> = _gameScore.asStateFlow()

    private val _heatmapEnabled = MutableStateFlow(false)
    val heatmapEnabled = _heatmapEnabled.asStateFlow()

    private val _heatmapIntensities = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val heatmapIntensities = _heatmapIntensities.asStateFlow()

    private val _events = Channel<CheckerEffect>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var recomputeJob: Job? = null

    init {
        try {
            val route = savedStateHandle.toRoute<CheckerRoute>()
            if (route.numbers.isNotEmpty()) {
                replaceNumbers(route.numbers.toSet())
            }
        } catch (e: IllegalArgumentException) {
            // Fallback or ignore if arguments not present/parseable
            logger.warning(TAG, "Invalid route args: ${e.message}")
        } catch (e: RuntimeException) {
            // Fallback or ignore if arguments not present/parseable
            logger.warning(TAG, "Runtime error parsing route args: ${e.message}")
        }
    }

    fun onEvent(event: CheckerUiEvent) {
        when (event) {
            is CheckerUiEvent.ToggleNumber -> toggleNumber(event.number)
            is CheckerUiEvent.ClearNumbers -> clearNumbers()
            is CheckerUiEvent.CheckGame -> checkGame()
            is CheckerUiEvent.RequestSave -> requestSaveConfirmation()
            is CheckerUiEvent.ConfirmSave -> confirmSaveGame()
        }
    }

    private fun toggleNumber(n: Int) {
        val next = _selectedNumbers.value.toMutableSet().apply {
            if (contains(n)) remove(n) else add(n)
        }.coerceToMax(GameConstants.GAME_SIZE)

        _selectedNumbers.value = next
        _isGameComplete.value = next.size == GameConstants.GAME_SIZE
        resetAnalysis()
        _uiState.value = CheckerUiState.Idle
    }

    private fun clearNumbers() {
        _selectedNumbers.value = emptySet()
        _isGameComplete.value = false
        resetAnalysis()
        _uiState.value = CheckerUiState.Idle
    }

    private fun saveGame() {
        val numbers = _selectedNumbers.value
        if (numbers.size != GameConstants.GAME_SIZE) {
            sendMessage(R.string.checker_incomplete_selection)
            return
        }
        // Use externalScope to prevent cancellation if user navigates away
        externalScope.launch {
            val game = LotofacilGame.fromNumbers(numbers)
            when (val result = saveGameUseCase(game)) {
                is AppResult.Success -> {
                    _events.trySend(CheckerEffect.ShowMessage(R.string.checker_game_saved))
                }
                is AppResult.Failure -> {
                    val error = result.error
                    logger.error(TAG, "Failed to save game: $error", null)
                    _events.trySend(CheckerEffect.ShowMessage(R.string.checker_save_fail_message))
                }
            }
        }
    }

    private fun requestSaveConfirmation() {
        if (_selectedNumbers.value.size != GameConstants.GAME_SIZE) {
            sendMessage(R.string.checker_incomplete_selection)
            return
        }
        _events.trySend(CheckerEffect.RequestSaveConfirmation)
    }

    private fun checkGame() {
        val numbers = _selectedNumbers.value
        if (numbers.size != GameConstants.GAME_SIZE) {
            sendMessage(R.string.checker_incomplete_selection)
            return
        }
        resetAnalysis()
        viewModelScope.launchCatching(
             onError = { _uiState.value = CheckerUiState.Error(R.string.error_check_game_failed) }
        ) {
            _uiState.value = CheckerUiState.Loading
            when (val result = checkGameUseCase(numbers.toSet()).first()) {
                is AppResult.Success -> {
                    val report = result.value
                    val lastDraw = historyRepository.getLastDraw()
                    val metrics = metricsCalculator.calculate(
                        LotofacilGame.fromNumbers(numbers),
                        lastDraw?.numbers
                    )
                    computeAnalysis(numbers)
                    _uiState.value = CheckerUiState.Success(report, metrics)

                    externalScope.launch {
                        try {
                            checkRunRepository.saveCheckRun(report)
                        } catch (e: IOException) {
                            logger.warning(TAG, "Network error saving check run telemetry: ${e.message}")
                        } catch (e: Exception) {
                            logger.warning(TAG, "Database error saving check run telemetry: ${e.message}")
                        }
                    }
                }
                is AppResult.Failure -> {
                    val msg = result.error.toUserMessageRes()
                    _uiState.value = CheckerUiState.Error(msg)
                }
            }
        }
    }

    private fun confirmSaveGame() {
        saveGame()
    }

    private fun replaceNumbers(newNumbers: Set<Int>) {
        _selectedNumbers.value = newNumbers.coerceToMax(GameConstants.GAME_SIZE)
        _isGameComplete.value = _selectedNumbers.value.size == GameConstants.GAME_SIZE
        resetAnalysis()
        _uiState.value = CheckerUiState.Idle
    }

    private fun resetAnalysis() {
        recomputeJob?.cancel()
        _gameScore.value = null
        _heatmapEnabled.value = false
        _heatmapIntensities.value = emptyMap()
    }

    private fun computeAnalysis(numbers: Set<Int>) {
        if (numbers.size != GameConstants.GAME_SIZE) {
            resetAnalysis()
            return
        }

        recomputeJob?.cancel()
        recomputeJob = viewModelScope.launchCatching {
            val (score, intensities) = withContext(defaultDispatcher) {
                val history = historyRepository.getHistory()
                val lastDraw = history.firstOrNull()

                val computedScore = metricsCalculator.analyze(
                    LotofacilGame.fromNumbers(numbers),
                    lastDraw?.numbers
                )

                val computedIntensities = if (history.isNotEmpty()) {
                    val totalDraws = history.size
                    val counts = IntArray(26) // 1..25
                    history.forEach { draw ->
                        MaskUtils.forEachNumber(draw.mask) { n ->
                            if (n in 1..25) counts[n]++
                        }
                    }
                    numbers.associateWith { n -> (counts[n].toFloat() / totalDraws).coerceIn(0f, 1f) }
                } else {
                    numbers.associateWith { 0.5f }
                }

                computedScore to computedIntensities
            }

            _gameScore.value = score
            _heatmapIntensities.value = intensities
            _heatmapEnabled.value = true
        }
    }
    private fun sendMessage(@StringRes messageResId: Int) {
        _events.trySend(CheckerEffect.ShowMessage(messageResId))
    }

    private fun Set<Int>.coerceToMax(max: Int): Set<Int> {
        return if (size > max) this.take(max).toSet() else this
    }

    override fun onCleared() {
        recomputeJob?.cancel()
        _events.close()
        super.onCleared()
    }
}

sealed interface CheckerUiEvent {
    data class ToggleNumber(val number: Int) : CheckerUiEvent
    data object ClearNumbers : CheckerUiEvent
    data object CheckGame : CheckerUiEvent
    data object RequestSave : CheckerUiEvent
    data object ConfirmSave : CheckerUiEvent
}

/**
 * Effects emitted by the Checker screen to be handled by the UI (one-off).
 * (Previously named CheckerEvent)
 */
sealed interface CheckerEffect {
    data class ShowMessage(@get:StringRes val messageResId: Int) : CheckerEffect
    data object RequestSaveConfirmation : CheckerEffect
}

@androidx.compose.runtime.Immutable
sealed interface CheckerUiState {
    data object Idle : CheckerUiState
    data object Loading : CheckerUiState
    data class Success(
        val report: CheckReport,
        val metrics: GameComputedMetrics
    ) : CheckerUiState
    data class Error(@param:StringRes val messageResId: Int) : CheckerUiState
}
