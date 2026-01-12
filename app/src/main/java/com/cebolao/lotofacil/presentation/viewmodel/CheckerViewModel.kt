package com.cebolao.lotofacil.presentation.viewmodel

import android.database.sqlite.SQLiteException
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.GameScore
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.CheckRunRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameMetricsCalculator
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import androidx.navigation.toRoute
import com.cebolao.lotofacil.navigation.CheckerRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * Dependencies for CheckerViewModel grouped to reduce constructor parameter count
 */
data class CheckerViewModelDependencies(
    val checkGameUseCase: CheckGameUseCase,
    val saveGameUseCase: SaveGameUseCase,
    val metricsCalculator: GameMetricsCalculator,
    val historyRepository: HistoryRepository,
    val checkRunRepository: CheckRunRepository,
    val logger: com.cebolao.lotofacil.domain.util.Logger
)

/**
 * Coroutine dependencies for CheckerViewModel
 */
data class CheckerViewModelCoroutineDependencies(
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher,
    @ApplicationScope val externalScope: CoroutineScope
)

/**
 * Events emitted by the Checker screen.
 */
@HiltViewModel
class CheckerViewModel @Inject constructor(
    dependencies: CheckerViewModelDependencies,
    coroutineDependencies: CheckerViewModelCoroutineDependencies,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val checkGameUseCase = dependencies.checkGameUseCase
    private val saveGameUseCase = dependencies.saveGameUseCase
    private val metricsCalculator = dependencies.metricsCalculator
    private val historyRepository = dependencies.historyRepository
    private val checkRunRepository = dependencies.checkRunRepository
    private val logger = dependencies.logger
    
    @DefaultDispatcher
    private val defaultDispatcher = coroutineDependencies.defaultDispatcher
    
    @ApplicationScope
    private val externalScope = coroutineDependencies.externalScope

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
            is CheckerUiEvent.RequestReplace -> requestReplaceConfirmation()
            is CheckerUiEvent.ConfirmReplace -> confirmReplaceNumbers()
            is CheckerUiEvent.CancelReplace -> cancelReplaceNumbers()
            is CheckerUiEvent.ToggleHeatmap -> toggleHeatmap()
        }
    }

    private fun toggleHeatmap() {
        _heatmapEnabled.value = !_heatmapEnabled.value
    }

    private fun toggleNumber(n: Int) {
        val next = _selectedNumbers.value.toMutableSet().apply {
            if (contains(n)) remove(n) else add(n)
        }.coerceToMax(GameConstants.GAME_SIZE)

        _selectedNumbers.value = next
        _isGameComplete.value = next.size == GameConstants.GAME_SIZE
        recomputeScoreAndHeatmap()
    }

    private fun clearNumbers() {
        _selectedNumbers.value = emptySet()
        _isGameComplete.value = false
        _gameScore.value = null
        _heatmapEnabled.value = false
        _heatmapIntensities.value = emptyMap()
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
                    _events.send(CheckerEffect.ShowMessage(R.string.checker_game_saved))
                }
                is AppResult.Failure -> {
                    val error = result.error
                    logger.error(TAG, "Failed to save game: $error", null)
                    _events.send(CheckerEffect.ShowMessage(R.string.checker_save_fail_message))
                }
            }
        }
    }

    private fun requestSaveConfirmation() {
        if (_selectedNumbers.value.size != GameConstants.GAME_SIZE) {
            sendMessage(R.string.checker_incomplete_selection)
            return
        }
        viewModelScope.launch {
            _events.send(CheckerEffect.RequestSaveConfirmation)
        }
    }

    private fun checkGame() {
        val numbers = _selectedNumbers.value
        if (numbers.size != GameConstants.GAME_SIZE) {
            sendMessage(R.string.checker_incomplete_selection)
            return
        }
        viewModelScope.launch {
            _uiState.value = CheckerUiState.Loading
            checkGameUseCase(numbers.toSet()).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        val report = result.value
                        val lastDraw = historyRepository.getLastDraw()
                        val metrics = metricsCalculator.calculate(
                            LotofacilGame.fromNumbers(numbers),
                            lastDraw?.numbers
                        )
                        _uiState.value = CheckerUiState.Success(report, metrics)

                        externalScope.launch {
                            try {
                                checkRunRepository.saveCheckRun(report)
                            } catch (e: IOException) {
                                logger.warning(TAG, "Network error saving check run telemetry: ${e.message}")
                            } catch (e: SQLiteException) {
                                logger.warning(TAG, "Database error saving check run telemetry: ${e.message}")
                            }
                        }
                    }
                    is AppResult.Failure -> {
                        _uiState.value = CheckerUiState.Error(R.string.error_check_game_failed)
                    }
                }
            }
        }
    }

    private fun requestReplaceConfirmation() {
        if (_selectedNumbers.value.isNotEmpty()) {
            viewModelScope.launch { 
                _events.send(CheckerEffect.RequestReplaceConfirmation) 
            }
        }
    }

    private fun confirmReplaceNumbers() {
        val generated = (1..25).shuffled().take(GameConstants.GAME_SIZE).toSet()
        replaceNumbers(generated)
    }

    private fun cancelReplaceNumbers() {
        // Intentional no-op
    }

    private fun confirmSaveGame() {
        saveGame()
    }

    private fun replaceNumbers(newNumbers: Set<Int>) {
        _selectedNumbers.value = newNumbers.coerceToMax(GameConstants.GAME_SIZE)
        _isGameComplete.value = _selectedNumbers.value.size == GameConstants.GAME_SIZE
        recomputeScoreAndHeatmap()
    }

    private fun recomputeScoreAndHeatmap() {
        val numbers = _selectedNumbers.value
        if (numbers.isEmpty()) {
            _gameScore.value = null
            _heatmapEnabled.value = false
            _heatmapIntensities.value = emptyMap()
            return
        }

        recomputeJob?.cancel()
        recomputeJob = viewModelScope.launch {
            val (score, intensities) = withContext(defaultDispatcher) {
                val history = historyRepository.getHistory()
                val lastDraw = history.firstOrNull()

                val computedScore = if (numbers.size == GameConstants.GAME_SIZE) {
                    metricsCalculator.analyze(
                        LotofacilGame.fromNumbers(numbers),
                        lastDraw?.numbers
                    )
                } else {
                    null
                }

                val computedIntensities = if (history.isNotEmpty()) {
                    val totalDraws = history.size
                    val counts = mutableMapOf<Int, Int>()
                    history.forEach { draw ->
                        draw.numbers.forEach { n ->
                            if (numbers.contains(n)) {
                                counts[n] = (counts[n] ?: 0) + 1
                            }
                        }
                    }

                    numbers.associateWith { n ->
                        val count = counts[n] ?: 0
                        (count.toFloat() / totalDraws).coerceIn(0f, 1f)
                    }
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

    private fun Set<Int>.coerceToMax(max: Int): Set<Int> {
        return if (size <= max) this else take(max).toSet()
    }

    private fun sendMessage(@StringRes messageResId: Int) {
        viewModelScope.launch {
            _events.send(CheckerEffect.ShowMessage(messageResId))
        }
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
    data object RequestReplace : CheckerUiEvent
    data object ConfirmReplace : CheckerUiEvent
    data object CancelReplace : CheckerUiEvent
    data object ToggleHeatmap : CheckerUiEvent
}

/**
 * Effects emitted by the Checker screen to be handled by the UI (one-off).
 * (Previously named CheckerEvent)
 */
sealed interface CheckerEffect {
    data class ShowMessage(@StringRes val messageResId: Int) : CheckerEffect
    data object RequestSaveConfirmation : CheckerEffect
    data object RequestReplaceConfirmation : CheckerEffect
}

sealed interface CheckerUiState {
    data object Idle : CheckerUiState
    data object Loading : CheckerUiState
    data class Success(
        val report: CheckReport,
        val metrics: GameComputedMetrics
    ) : CheckerUiState
    data class Error(@param:StringRes val messageResId: Int) : CheckerUiState
}
