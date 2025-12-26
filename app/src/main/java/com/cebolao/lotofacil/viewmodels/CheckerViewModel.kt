package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.GameAnalyzer
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.GameScore
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.CheckRunRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameMetricsCalculator
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.GetAnalyzedStatsUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.util.CHECKER_ARG_SEPARATOR
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CheckerUiState {
    data object Idle : CheckerUiState
    data object Loading : CheckerUiState
    data class Success(
        val report: CheckReport,
        val gameMetrics: GameComputedMetrics
    ) : CheckerUiState

    data class Error(@param:StringRes val messageResId: Int) : CheckerUiState
}

@HiltViewModel
class CheckerViewModel @Inject constructor(
    private val checkGameUseCase: CheckGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val checkRunRepository: CheckRunRepository,
    private val historyRepository: HistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val metricsCalculator = GameMetricsCalculator()

    private val _uiState = MutableStateFlow<CheckerUiState>(CheckerUiState.Idle)
    val uiState: StateFlow<CheckerUiState> = _uiState.asStateFlow()

    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers: StateFlow<Set<Int>> = _selectedNumbers.asStateFlow()

    private val _gameScore = MutableStateFlow<GameScore?>(null)
    val gameScore: StateFlow<GameScore?> = _gameScore.asStateFlow()

    val isGameComplete: StateFlow<Boolean> = _selectedNumbers
        .map { it.size == GameConstants.GAME_SIZE }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            false
        )

    private val _heatmapEnabled = MutableStateFlow(false)
    val heatmapEnabled = _heatmapEnabled.asStateFlow()

    private val _heatmapIntensities = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val heatmapIntensities = _heatmapIntensities.asStateFlow()

    private val _events = Channel<Int>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        savedStateHandle.get<String>(Screen.Checker.ARG_NUMBERS)?.let { arg ->
            val numbers = arg
                .split(CHECKER_ARG_SEPARATOR)
                .mapNotNull { it.toIntOrNull() }
                .filter { it in GameConstants.NUMBER_RANGE }
                .toSet()

            if (numbers.isNotEmpty()) {
                _selectedNumbers.value = numbers
            }
        }
        
        loadHeatmapData()
    }
    
    fun toggleHeatmap() {
        _heatmapEnabled.update { !it }
    }

    fun toggleNumber(number: Int) {
        _selectedNumbers.update { current ->
            when {
                number in current -> current - number
                current.size < GameConstants.GAME_SIZE -> current + number
                else -> current
            }
        }
        
        if (_gameScore.value != null) _gameScore.value = null
        if (_uiState.value !is CheckerUiState.Idle) _uiState.value = CheckerUiState.Idle
    }

    fun clearNumbers() {
        _selectedNumbers.value = emptySet()
        _gameScore.value = null
        _uiState.value = CheckerUiState.Idle
    }
    
    private fun analyzeScore(numbers: Set<Int>) {
        _gameScore.value = GameAnalyzer.analyze(numbers)
    }
    
    private fun loadHeatmapData() {
        viewModelScope.launch {
            getAnalyzedStatsUseCase(25)
                .onSuccess { report ->
                    val freqs = report.mostFrequentNumbers
                    if (freqs.isNotEmpty()) {
                        val max = freqs.maxOf { it.frequency }.toFloat()
                        val min = freqs.minOf { it.frequency }.toFloat()
                        val range = (max - min).coerceAtLeast(1f)
                        
                        val map = freqs.associate { 
                            it.number to ((it.frequency - min) / range)
                        }
                        _heatmapIntensities.value = map
                    }
                }
        }
    }


    fun checkGame() {
        if (_uiState.value is CheckerUiState.Loading) return

        if (_selectedNumbers.value.size != GameConstants.GAME_SIZE) {
            sendEvent(R.string.checker_incomplete_game_message)
            return
        }

        val numbers = _selectedNumbers.value
        viewModelScope.launch {
            _uiState.value = CheckerUiState.Loading
            analyzeScore(numbers)
            
            checkGameUseCase(numbers)
                .collect { result ->
                    result.onSuccess { report ->
                        val game = report.ticket
                        
                        // Fetch last draw for 'Repeated' calculation
                        val lastDraw = historyRepository.getLastDraw()
                        
                        val metrics = metricsCalculator.calculate(game, lastDraw?.numbers)
                        
                        try {
                            checkRunRepository.saveCheckRun(report)
                        } catch (e: Exception) {
                            android.util.Log.e("CheckerViewModel", "Failed to save check run", e)
                        }
                        
                        _uiState.value = CheckerUiState.Success(
                            report = report,
                            gameMetrics = metrics
                        )
                    }.onFailure {
                        _uiState.value = CheckerUiState.Error(R.string.error_analysis_failed)
                    }
                }
        }
    }

    fun saveGame() {
        if (_selectedNumbers.value.size != GameConstants.GAME_SIZE) return

        val numbers = _selectedNumbers.value
        viewModelScope.launch {
            saveGameUseCase(LotofacilGame.fromNumbers(numbers))
                .onSuccess { sendEvent(R.string.checker_save_success_message) }
                .onFailure { sendEvent(R.string.checker_save_fail_message) }
        }
    }

    private fun sendEvent(@StringRes resId: Int) {
        viewModelScope.launch {
            _events.send(resId)
        }
    }
}
