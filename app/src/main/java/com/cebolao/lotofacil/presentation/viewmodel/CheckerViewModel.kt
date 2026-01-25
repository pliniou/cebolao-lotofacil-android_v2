package com.cebolao.lotofacil.presentation.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
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
import com.cebolao.lotofacil.domain.model.MaskUtils
import com.cebolao.lotofacil.domain.repository.CheckRunRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameMetricsCalculator
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import androidx.navigation.toRoute
import com.cebolao.lotofacil.navigation.AppRoute
import com.cebolao.lotofacil.presentation.util.UiEvent
import com.cebolao.lotofacil.presentation.util.UiState
import com.cebolao.lotofacil.util.launchCatching
import com.cebolao.lotofacil.util.toUserMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@androidx.compose.runtime.Immutable
data class CheckerUiState(
    val status: CheckerScreenStatus = CheckerScreenStatus.Idle,
    val selectedNumbers: Set<Int> = emptySet(),
    val isGameComplete: Boolean = false,
    val gameScore: GameScore? = null,
    val heatmapEnabled: Boolean = false,
    val heatmapIntensities: Map<Int, Float> = emptyMap()
) : UiState

sealed interface CheckerScreenStatus {
    data object Idle : CheckerScreenStatus
    data object Loading : CheckerScreenStatus
    data class Success(
        val report: CheckReport,
        val metrics: GameComputedMetrics
    ) : CheckerScreenStatus

    data class Error(@param:StringRes val messageResId: Int) : CheckerScreenStatus
}

@HiltViewModel
class CheckerViewModel @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val metricsCalculator: GameMetricsCalculator,
    private val historyRepository: HistoryRepository,
    private val checkRunRepository: CheckRunRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val externalScope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    companion object {
        private const val TAG = "CheckerViewModel"
    }

    private val _uiState = MutableStateFlow(CheckerUiState())
    val uiState: StateFlow<CheckerUiState> = _uiState.asStateFlow()

    private val _events = Channel<CheckerEffect>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var recomputeJob: Job? = null

    init {
        try {
            // First try to restore saved selected numbers (survives process death)
            val saved = savedStateHandle.get<List<Int>>("checker_selected_numbers")
            if (saved != null && saved.isNotEmpty()) {
                replaceNumbers(saved.toSet())
            } else {
                val route = savedStateHandle.toRoute<AppRoute.Checker>()
                if (route.numbers.isNotEmpty()) {
                    replaceNumbers(route.numbers.toSet())
                }
            }
            // If navigation passed numbers via SavedStateHandle (from bottom-tab style navigation),
            // consume them here and remove the key so they don't persist unexpectedly.
            val pending = savedStateHandle.get<List<Int>>("checker_numbers")
            if (pending != null && pending.isNotEmpty()) {
                replaceNumbers(pending.toSet())
                savedStateHandle.remove<List<Int>>("checker_numbers")
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid route args: ${e.message}")
        } catch (e: RuntimeException) {
            Log.w(TAG, "Runtime error parsing route args: ${e.message}")
        }
    }

    fun onEvent(event: CheckerUiEvent) {
        when (event) {
            is CheckerUiEvent.ToggleNumber -> toggleNumber(event.number)
            CheckerUiEvent.ClearNumbers -> clearNumbers()
            CheckerUiEvent.CheckGame -> checkGame()
            CheckerUiEvent.RequestSave -> requestSaveConfirmation()
            CheckerUiEvent.ConfirmSave -> confirmSaveGame()
        }
    }

    private fun toggleNumber(n: Int) {
        val next = _uiState.value.selectedNumbers.toMutableSet().apply {
            if (contains(n)) remove(n) else add(n)
        }.coerceToMax(GameConstants.GAME_SIZE)

        _uiState.update {
            it.copy(
                selectedNumbers = next,
                isGameComplete = next.size == GameConstants.GAME_SIZE,
                status = CheckerScreenStatus.Idle,
                gameScore = null,
                heatmapEnabled = false,
                heatmapIntensities = emptyMap()
            )
        }
        // Persist selection so it survives process death and nav restore
        savedStateHandle.set("checker_selected_numbers", next.sorted())
    }

    private fun clearNumbers() {
        _uiState.update {
            it.copy(
                selectedNumbers = emptySet(),
                isGameComplete = false,
                status = CheckerScreenStatus.Idle,
                gameScore = null,
                heatmapEnabled = false,
                heatmapIntensities = emptyMap()
            )
        }
        savedStateHandle.remove<List<Int>>("checker_selected_numbers")
    }

    private fun saveGame() {
        val numbers = _uiState.value.selectedNumbers
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
                    Log.e(TAG, "Failed to save game: ${result.error}")
                    _events.trySend(CheckerEffect.ShowMessage(R.string.checker_save_fail_message))
                }
            }
        }
    }

    private fun requestSaveConfirmation() {
        if (_uiState.value.selectedNumbers.size != GameConstants.GAME_SIZE) {
            sendMessage(R.string.checker_incomplete_selection)
            return
        }
        _events.trySend(CheckerEffect.RequestSaveConfirmation)
    }

    private fun checkGame() {
        val numbers = _uiState.value.selectedNumbers
        if (numbers.size != GameConstants.GAME_SIZE) {
            sendMessage(R.string.checker_incomplete_selection)
            return
        }
        resetAnalysis()
        viewModelScope.launchCatching(
            onError = { _uiState.update { it.copy(status = CheckerScreenStatus.Error(R.string.error_check_game_failed)) } }
        ) {
            _uiState.update { it.copy(status = CheckerScreenStatus.Loading) }
            when (val result = checkGameUseCase(numbers).first()) {
                is AppResult.Success -> {
                    val report = result.value
                    val lastDraw = when (val lastDrawResult = historyRepository.getLastDraw()) {
                        is AppResult.Success -> lastDrawResult.value
                        is AppResult.Failure -> null
                    }
                    val metrics = metricsCalculator.calculate(
                        LotofacilGame.fromNumbers(numbers),
                        lastDraw?.numbers
                    )
                    computeAnalysis(numbers)
                    _uiState.update { it.copy(status = CheckerScreenStatus.Success(report, metrics)) }

                    externalScope.launch {
                        try {
                            checkRunRepository.saveCheckRun(report)
                        } catch (e: IOException) {
                            Log.w(TAG, "Network error saving check run telemetry: ${e.message}")
                        } catch (e: Exception) {
                            Log.w(TAG, "Database error saving check run telemetry: ${e.message}")
                        }
                    }
                }
                is AppResult.Failure -> {
                    val msg = result.error.toUserMessageRes()
                    _uiState.update { it.copy(status = CheckerScreenStatus.Error(msg)) }
                }
            }
        }
    }

    private fun confirmSaveGame() {
        saveGame()
    }

    private fun replaceNumbers(newNumbers: Set<Int>) {
        val next = newNumbers.coerceToMax(GameConstants.GAME_SIZE)
        _uiState.update {
            it.copy(
                selectedNumbers = next,
                isGameComplete = next.size == GameConstants.GAME_SIZE,
                status = CheckerScreenStatus.Idle,
                gameScore = null,
                heatmapEnabled = false,
                heatmapIntensities = emptyMap()
            )
        }
        savedStateHandle.set("checker_selected_numbers", next.sorted())
    }

    private fun resetAnalysis() {
        recomputeJob?.cancel()
        _uiState.update { it.copy(gameScore = null, heatmapEnabled = false, heatmapIntensities = emptyMap()) }
    }

    private fun computeAnalysis(numbers: Set<Int>) {
        if (numbers.size != GameConstants.GAME_SIZE) {
            resetAnalysis()
            return
        }

        recomputeJob?.cancel()
        recomputeJob = viewModelScope.launchCatching {
            val (score, intensities) = withContext(defaultDispatcher) {
                val history = when (val historyResult = historyRepository.getHistory()) {
                    is AppResult.Success -> historyResult.value
                    is AppResult.Failure -> emptyList()
                }
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

            _uiState.update {
                it.copy(
                    gameScore = score,
                    heatmapIntensities = intensities,
                    heatmapEnabled = true
                )
            }
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

sealed interface CheckerUiEvent : UiEvent {
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
