package com.cebolao.lotofacil.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterPreset
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.service.GenerationFailureReason
import com.cebolao.lotofacil.domain.service.GenerationProgressType
import com.cebolao.lotofacil.domain.service.GenerationStep
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import com.cebolao.lotofacil.domain.usecase.GetLastDrawUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGeneratedGamesUseCase
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.util.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
        val formatArgs: List<Any> = emptyList()
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
    val showResetDialog: Boolean = false,
    val filterInfoToShow: FilterType? = null,
    val showStrictConfirmation: Boolean = false
)

/**
 * ViewModel for the Filters/Generator screen.
 * Manages filter configuration, game generation, and preset strategies.
 */
@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val generateGamesUseCase: GenerateGamesUseCase,
    private val getLastDrawUseCase: GetLastDrawUseCase,
    private val saveGeneratedGamesUseCase: SaveGeneratedGamesUseCase
) : BaseViewModel() {
    private val _filterStates = MutableStateFlow(FilterType.defaults())
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    private val _lastDraw = MutableStateFlow<Set<Int>?>(null)
    private val _showResetDialog = MutableStateFlow(false)
    private val _filterInfoToShow = MutableStateFlow<FilterType?>(null)
    private val _showStrictConfirmation = MutableStateFlow(false)
    private val _events = Channel<NavigationEvent>(Channel.BUFFERED)

    /**
     * Flow of one-time navigation events to be consumed by the UI.
     */
    val events = _events.receiveAsFlow()

    private var generationJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = getLastDrawUseCase()) {
                is AppResult.Success -> _lastDraw.value = result.value?.numbers
                is AppResult.Failure -> _lastDraw.value = null
            }
        }
    }

    private data class FiltersPartialState(
        val filterStates: List<FilterState>,
        val generationState: GenerationUiState,
        val lastDraw: Set<Int>?,
        val showResetDialog: Boolean,
        val filterInfoToShow: FilterType?
    )

    val uiState: StateFlow<FiltersScreenState> = combine(
        _filterStates,
        _generationState,
        _lastDraw,
        _showResetDialog,
        _filterInfoToShow
    ) { filterStates, generationState, lastDraw, showResetDialog, filterInfoToShow ->
        FiltersPartialState(
            filterStates = filterStates,
            generationState = generationState,
            lastDraw = lastDraw,
            showResetDialog = showResetDialog,
            filterInfoToShow = filterInfoToShow
        )
    }.combine(_showStrictConfirmation) { partial, showStrictConfirmation ->
        FiltersScreenState(
            filterStates = partial.filterStates,
            generationState = partial.generationState,
            lastDraw = partial.lastDraw,
            showResetDialog = partial.showResetDialog,
            filterInfoToShow = partial.filterInfoToShow,
            showStrictConfirmation = showStrictConfirmation
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        FiltersScreenState()
    )

    private fun showResetDialog() {
        _showResetDialog.value = true
    }

    fun onEvent(event: FiltersUiEvent) {
        when (event) {
            is FiltersUiEvent.ToggleFilter -> toggleFilter(event.type, event.enabled)
            is FiltersUiEvent.AdjustRange -> adjustRange(event.type, event.range)
            is FiltersUiEvent.ApplyPreset -> applyPreset(event.preset)
            is FiltersUiEvent.GenerateGames -> generateGames(event.quantity)
            FiltersUiEvent.CancelGeneration -> cancelGeneration()
            FiltersUiEvent.RequestResetFilters -> showResetDialog()
            FiltersUiEvent.ConfirmResetFilters -> confirmResetFilters()
            FiltersUiEvent.DismissResetDialog -> dismissResetDialog()
            is FiltersUiEvent.ShowFilterInfo -> showFilterInfo(event.type)
            FiltersUiEvent.DismissFilterInfo -> dismissFilterInfo()
            FiltersUiEvent.ConfirmStrictGeneration -> confirmStrictGeneration()
            FiltersUiEvent.DismissStrictConfirmation -> dismissStrictConfirmation()
        }
    }

    private fun toggleFilter(type: FilterType, enabled: Boolean) {
        _filterStates.update { current ->
            current.map { if (it.type == type) it.copy(isEnabled = enabled) else it }
        }
    }

    private fun adjustRange(type: FilterType, range: ClosedFloatingPointRange<Float>) {
        _filterStates.update { current ->
            current.map {
                if (it.type == type) it.copy(selectedRange = range.snapToStep(it.type.fullRange)) else it
            }
        }
    }

    private fun applyPreset(preset: FilterPreset) {
        val rules = preset.rules
        _filterStates.update { current ->
            current.map { filter ->
                val newRange = rules[filter.type]
                if (newRange != null) {
                    filter.copy(
                        selectedRange = newRange.snapToStep(filter.type.fullRange),
                        isEnabled = true
                    )
                } else {
                    filter
                }
            }
        }
        _events.trySend(NavigationEvent.ShowSnackbar(R.string.filter_preset_applied, formatArgs = listOf(preset.labelRes)))
    }

    private var pendingQuantity: Int = 10

    private fun generateGames(quantity: Int) {
         if (_generationState.value is GenerationUiState.Loading) return
         
         val filters = _filterStates.value.filter { it.isEnabled }
         
         // Simple heuristic to check strictness
         val activeFilters = filters.size
         val isVeryStrict = activeFilters >= 5
         
         if (isVeryStrict && !_showStrictConfirmation.value) {
             pendingQuantity = quantity
             _showStrictConfirmation.value = true
             return
         }

         startGeneration(quantity, filters)
    }

    private fun startGeneration(quantity: Int, filters: List<FilterState>) {
        generationJob?.cancel()
        generationJob = viewModelScope.launchCatching(
            onError = {
                _generationState.value = GenerationUiState.Error(R.string.filters_generation_error)
            }
        ) {
            _generationState.value = GenerationUiState.Loading(0, quantity)
            
            // Debouncer for UI updates
            var lastProgressUpdate = 0L

            generateGamesUseCase(quantity, filters).collect { result ->
                when (result) {
                    is AppResult.Failure -> {
                        _generationState.value = GenerationUiState.Error(R.string.filters_generation_error)
                    }
                    is AppResult.Success -> {
                        val progress = result.value
                        val type = progress.progressType
                        when (type) {
                            is GenerationProgressType.Step -> {
                                val now = System.currentTimeMillis()
                                // Update progress roughly every 100ms or so to keep UI responsive without overhead
                                if (now - lastProgressUpdate > 100) {
                                    val msgRes = when (type.step) {
                                        GenerationStep.RANDOM_START -> R.string.game_generator_random_start
                                        GenerationStep.HEURISTIC_START -> R.string.game_generator_heuristic_start
                                        GenerationStep.RANDOM_FALLBACK -> R.string.game_generator_random_fallback
                                    }
                                    _generationState.value = GenerationUiState.Loading(progress.current, quantity, msgRes)
                                    lastProgressUpdate = now
                                }
                            }

                            is GenerationProgressType.Finished -> {
                                val games = type.games

                                // Save games
                                when (val saveResult = saveGeneratedGamesUseCase(games)) {
                                    is AppResult.Success -> {
                                        _generationState.value = GenerationUiState.Success(games.size)
                                        _events.trySend(NavigationEvent.NavigateToGeneratedGames)
                                    }

                                    is AppResult.Failure -> {
                                        _generationState.value = GenerationUiState.Error(R.string.filters_save_error)
                                    }
                                }
                            }

                            is GenerationProgressType.Failed -> {
                                val msg = when (type.reason) {
                                    GenerationFailureReason.FILTERS_TOO_STRICT -> R.string.game_generator_failure_filters_strict
                                    GenerationFailureReason.NO_HISTORY -> R.string.game_generator_failure_no_history
                                    else -> R.string.game_generator_failure_generic
                                }
                                _generationState.value = GenerationUiState.Error(msg)
                            }

                            else -> {
                                /* Started, Attempt - ignore for UI state */
                            }
                        }
                    }
                }
            }
        }
    }

    private fun confirmResetFilters() {
        _showResetDialog.value = false
        _filterStates.value = FilterType.defaults()
        _events.trySend(NavigationEvent.ShowSnackbar(R.string.filters_reset_success))
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

    private fun confirmStrictGeneration() {
        _showStrictConfirmation.value = false
        val filters = _filterStates.value.filter { it.isEnabled }
        startGeneration(pendingQuantity, filters)
    }

    private fun dismissStrictConfirmation() {
        _showStrictConfirmation.value = false
    }

    private fun cancelGeneration() {
        generationJob?.cancel()
        generationJob = null
        _generationState.value = GenerationUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        cancelGeneration()
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
    data object ConfirmStrictGeneration : FiltersUiEvent
    data object DismissStrictConfirmation : FiltersUiEvent
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
