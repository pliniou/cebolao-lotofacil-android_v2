package com.cebolao.lotofacil.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.service.FilterSuccessCalculator
import com.cebolao.lotofacil.domain.service.GenerationFailureReason
import com.cebolao.lotofacil.domain.service.GenerationProgressType
import com.cebolao.lotofacil.domain.service.GenerationTelemetry
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import com.cebolao.lotofacil.domain.usecase.GetLastDrawUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGeneratedGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Events emitted by the Filters screen for navigation and user feedback.
 */
sealed interface NavigationEvent {
    /**
     * Event to display a snackbar message.
     */
    data class ShowSnackbar(
        @param:StringRes val messageRes: Int,
        @param:StringRes val labelRes: Int? = null
    ) : NavigationEvent

    /**
     * Event to navigate to the generated games screen.
     */
    data object NavigateToGeneratedGames : NavigationEvent
}

/**
 * Represents the state of the game generation process.
 */
sealed interface GenerationUiState {
    /** No generation in progress. */
    data object Idle : GenerationUiState

    /**
     * Generation is currently running.
     */
    data class Loading(
        val progress: Int = 0,
        val total: Int = 0,
        @param:StringRes val messageRes: Int = R.string.filters_button_generating
    ) : GenerationUiState

    /**
     * Generation completed successfully.
     */
    data class Success(val totalGenerated: Int) : GenerationUiState

    /**
     * Generation failed with an error.
     */
    data class Error(@param:StringRes val messageRes: Int) : GenerationUiState
}

/**
 * UI state for the Filters screen.
 */
@androidx.compose.runtime.Immutable
data class FiltersScreenState(
    val filterStates: List<FilterState> = emptyList(),
    val generationState: GenerationUiState = GenerationUiState.Idle,
    val lastDraw: Set<Int>? = null,
    val successProbability: Float = 1f,
    val showResetDialog: Boolean = false,
    val filterInfoToShow: FilterType? = null,
    val generationTelemetry: GenerationTelemetry? = null
)

/**
 * ViewModel for the Filters/Generator screen.
 * Manages filter configuration, game generation, and preset strategies.
 */
@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val generateGames: GenerateGamesUseCase,
    private val getLastDraw: GetLastDrawUseCase,
    private val filterSuccessCalculator: FilterSuccessCalculator,
    private val saveGeneratedGamesUseCase: SaveGeneratedGamesUseCase
) : ViewModel() {
    private val _filterStates = MutableStateFlow(FilterType.defaults())
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    private val _lastDraw = MutableStateFlow<Set<Int>?>(null)
    private val _showResetDialog = MutableStateFlow(false)
    private val _filterInfoToShow = MutableStateFlow<FilterType?>(null)
    private val _generationTelemetry = MutableStateFlow<GenerationTelemetry?>(null)
    private val _events = Channel<NavigationEvent>(Channel.BUFFERED)

    /**
     * Flow of one-time navigation events to be consumed by the UI.
     */
    val events = _events.receiveAsFlow()

    private var generationJob: Job? = null
    private var debounceJob: Job? = null

    /**
     * Estimated success probability based on current filter configuration (0-1).
     */
    private val successProbability: StateFlow<Float> =
        _filterStates
            .map { filters -> filterSuccessCalculator(filters.filter { it.isEnabled }) }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1f)

    /**
     * Combined UI state for the Filters screen.
     */
    val uiState: StateFlow<FiltersScreenState> = combine(
        listOf(
            _filterStates,
            _generationState,
            _lastDraw,
            successProbability,
            _showResetDialog,
            _filterInfoToShow,
            _generationTelemetry
        )
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        FiltersScreenState(
            filterStates = values[0] as List<FilterState>,
            generationState = values[1] as GenerationUiState,
            lastDraw = values[2] as? Set<Int>?,
            successProbability = values[3] as Float,
            showResetDialog = values[4] as Boolean,
            filterInfoToShow = values[5] as? FilterType?,
            generationTelemetry = values[6] as? GenerationTelemetry?
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FiltersScreenState()
    )

    init {
        viewModelScope.launch {
            when (val result = getLastDraw()) {
                is AppResult.Success -> {
                    _lastDraw.value = result.value?.numbers?.toSet()
                }
                is AppResult.Failure -> {
                    // Ignore failure for last draw in filters (optional)
                }
            }
        }
    }

    fun onEvent(event: FiltersUiEvent) {
        when (event) {
            is FiltersUiEvent.ToggleFilter -> onFilterToggle(event.type, event.enabled)
            is FiltersUiEvent.AdjustRange -> onRangeAdjust(event.type, event.range)
            is FiltersUiEvent.ApplyPreset -> applyPreset(event.preset)
            is FiltersUiEvent.GenerateGames -> generateGames(event.quantity)
            is FiltersUiEvent.CancelGeneration -> cancelGeneration()
            is FiltersUiEvent.RequestResetFilters -> showResetDialog()
            is FiltersUiEvent.ConfirmResetFilters -> confirmResetFilters()
            is FiltersUiEvent.DismissResetDialog -> dismissResetDialog()
            is FiltersUiEvent.ShowFilterInfo -> showFilterInfo(event.type)
            is FiltersUiEvent.DismissFilterInfo -> dismissFilterInfo()
        }
    }

    private fun onFilterToggle(type: FilterType, enabled: Boolean) {
        _filterStates.update { current ->
            current.map {
                if (it.type == type) it.copy(isEnabled = enabled) else it
            }
        }
    }

    private fun onRangeAdjust(type: FilterType, range: ClosedFloatingPointRange<Float>) {
        debounceJob?.cancel()

        debounceJob = viewModelScope.launch {
            delay(150) // Debounce for 150ms

            _filterStates.update { current ->
                current.map {
                    if (it.type == type) {
                        val snapped = range.snapToStep(it.type.fullRange)
                        it.copy(selectedRange = snapped, isEnabled = true)
                    } else it
                }
            }
        }
    }

    private fun applyPreset(preset: FilterPreset) {
        _filterStates.update { current ->
            current.map { state ->
                val rule = preset.rules[state.type]
                if (rule != null) state.copy(isEnabled = true, selectedRange = rule)
                else state.copy(isEnabled = false, selectedRange = state.type.defaultRange)
            }
        }

        viewModelScope.launch {
            _events.send(
                NavigationEvent.ShowSnackbar(
                    messageRes = R.string.filter_preset_applied,
                    labelRes = preset.labelRes
                )
            )
        }
    }

    private fun generateGames(quantity: Int) {
        if (generationJob != null) return

        _generationState.value = GenerationUiState.Loading()
        _generationTelemetry.value = GenerationTelemetry.start()

        generationJob = viewModelScope.launch {
            var hasFailed = false
            try {
                var totalGenerated = 0
                generateGames(
                    quantity = quantity,
                    filters = _filterStates.value.filter { it.isEnabled }
                ).collect { progress ->
                    when (val type = progress.progressType) {
                        is GenerationProgressType.Started -> {
                            _generationState.value = GenerationUiState.Loading(0, progress.total)
                        }
                        is GenerationProgressType.Step -> {
                            _generationState.value = GenerationUiState.Loading(progress.current, progress.total)
                        }
                        is GenerationProgressType.Attempt -> {
                            _generationState.value = GenerationUiState.Loading(progress.current, progress.total)
                            totalGenerated = progress.current
                        }
                        is GenerationProgressType.Finished -> {
                            totalGenerated = type.games.size
                            _generationTelemetry.value = type.telemetry
                            // Save generated games and navigate
                            if (type.games.isNotEmpty()) {
                                when (saveGeneratedGamesUseCase(type.games)) {
                                    is AppResult.Success -> {
                                        _events.send(NavigationEvent.NavigateToGeneratedGames)
                                    }
                                    is AppResult.Failure -> {
                                        hasFailed = true
                                        _generationState.value = GenerationUiState.Error(R.string.filters_save_error)
                                        currentCoroutineContext().cancel()
                                    }
                                }
                            }
                        }
                        is GenerationProgressType.Failed -> {
                            hasFailed = true
                            val errorMsg = when (type.reason) {
                                GenerationFailureReason.NO_HISTORY -> R.string.game_generator_failure_no_history
                                GenerationFailureReason.FILTERS_TOO_STRICT -> R.string.game_generator_failure_filters_strict
                                else -> R.string.game_generator_failure_generic
                            }
                            _generationState.value = GenerationUiState.Error(errorMsg)
                            currentCoroutineContext().cancel()
                        }
                    }
                }
                if (!hasFailed) {
                    _generationState.value = GenerationUiState.Success(totalGenerated)
                }
            } catch (_: CancellationException) {
                // Ignore cancellations
            } catch (_: Throwable) {
                _generationState.value = GenerationUiState.Error(R.string.filters_generation_error)
            } finally {
                generationJob = null
            }
        }
    }

    private fun cancelGeneration() {
        generationJob?.cancel()
        generationJob = null
        _generationState.value = GenerationUiState.Idle
    }

    private fun showResetDialog() {
        _showResetDialog.value = true
    }

    private fun confirmResetFilters() {
        _showResetDialog.value = false
        _filterStates.value = FilterType.defaults()
    }

    private fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    private fun showFilterInfo(type: FilterType) {
        _filterInfoToShow.value = type
    }

    private fun dismissFilterInfo() {
        _filterInfoToShow.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelGeneration()
        debounceJob?.cancel()
        _events.close()
    }
}

sealed interface FiltersUiEvent {
    data class ToggleFilter(val type: FilterType, val enabled: Boolean) : FiltersUiEvent
    data class AdjustRange(val type: FilterType, val range: ClosedFloatingPointRange<Float>) : FiltersUiEvent
    data class ApplyPreset(val preset: FilterPreset) : FiltersUiEvent
    data class GenerateGames(val quantity: Int) : FiltersUiEvent
    data object CancelGeneration : FiltersUiEvent
    data object RequestResetFilters : FiltersUiEvent
    data object ConfirmResetFilters : FiltersUiEvent
    data object DismissResetDialog : FiltersUiEvent
    data class ShowFilterInfo(val type: FilterType) : FiltersUiEvent
    data object DismissFilterInfo : FiltersUiEvent
}

/**
 * Snaps a floating point range to integer steps within the allowed full range.
 */
private fun ClosedFloatingPointRange<Float>.snapToStep(
    full: ClosedFloatingPointRange<Float>
): ClosedFloatingPointRange<Float> {
    val a = start.roundToInt().toFloat().coerceIn(full.start, full.endInclusive)
    val b = endInclusive.roundToInt().toFloat().coerceIn(full.start, full.endInclusive)

    val start = minOf(a, b)
    val end = maxOf(a, b)
    return start..end
}
