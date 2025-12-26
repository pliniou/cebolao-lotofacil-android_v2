package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.service.FilterSuccessCalculator
import com.cebolao.lotofacil.domain.service.GenerationFailureReason
import com.cebolao.lotofacil.domain.service.GenerationProgress
import com.cebolao.lotofacil.domain.service.GenerationProgressType
import com.cebolao.lotofacil.domain.service.GenerationStep
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import com.cebolao.lotofacil.domain.usecase.GetLastDrawUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGeneratedGamesUseCase
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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

sealed interface NavigationEvent {
    data object NavigateToGeneratedGames : NavigationEvent

    data class ShowSnackbar(
        @param:StringRes val messageRes: Int,
        @param:StringRes val labelRes: Int? = null
    ) : NavigationEvent
}

data class FiltersScreenState(
    val filterStates: List<FilterState> = emptyList(),
    val generationState: GenerationUiState = GenerationUiState.Idle,
    val lastDraw: Set<Int>? = null,
    val successProbability: Float = 1f,
    val showResetDialog: Boolean = false,
    val filterInfoToShow: FilterType? = null,
    val generationTelemetry: com.cebolao.lotofacil.domain.service.GenerationTelemetry? = null
)

sealed interface GenerationUiState {
    data object Idle : GenerationUiState

    data class Loading(
        @param:StringRes val messageRes: Int,
        val progress: Int = 0,
        val total: Int = 0
    ) : GenerationUiState
}

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val saveGeneratedGamesUseCase: SaveGeneratedGamesUseCase,
    private val generateGamesUseCase: GenerateGamesUseCase,
    private val filterSuccessCalculator: FilterSuccessCalculator,
    private val getLastDrawUseCase: GetLastDrawUseCase
) : ViewModel() {

    private val _filterStates = MutableStateFlow(FilterType.entries.map { FilterState(type = it) })
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    private val _lastDraw = MutableStateFlow<Set<Int>?>(null)
    private val _showResetDialog = MutableStateFlow(false)
    private val _filterInfoToShow = MutableStateFlow<FilterType?>(null)
    private val _generationTelemetry = MutableStateFlow<com.cebolao.lotofacil.domain.service.GenerationTelemetry?>(null)

    private val _events = Channel<NavigationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var generationJob: Job? = null

    /**
     * Recalcula somente quando os filtros mudam.
     */
    private val successProbability: StateFlow<Float> =
        _filterStates
            .map { filters -> filterSuccessCalculator(filters.filter { it.isEnabled }) }
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
                1f
            )

    /**
     * Compatível com versões antigas do coroutines: só usamos combine tipado até 4 flows,
     * e encadeamos o restante.
     */
    private val baseState: StateFlow<FiltersScreenState> = combine(
        _filterStates,
        _generationState,
        _lastDraw,
        successProbability
    ) { filters, genState, lastDraw, prob ->
        FiltersScreenState(
            filterStates = filters,
            generationState = genState,
            lastDraw = lastDraw,
            successProbability = prob,
            showResetDialog = false,
            filterInfoToShow = null
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        FiltersScreenState()
    )

    val uiState: StateFlow<FiltersScreenState> = combine(
        baseState,
        _showResetDialog,
        _filterInfoToShow,
        _generationTelemetry
    ) { base, showReset, info, telemetry ->
        base.copy(showResetDialog = showReset, filterInfoToShow = info, generationTelemetry = telemetry)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        FiltersScreenState()
    )

    init {
        viewModelScope.launch {
            getLastDrawUseCase()
                .onSuccess { draw -> _lastDraw.value = draw?.numbers }
                .onFailure { /* mantém null */ }
        }
    }

    fun onFilterToggle(type: FilterType, isEnabled: Boolean) {
        updateFilter(type) { it.copy(isEnabled = isEnabled) }
    }

    fun onRangeAdjust(type: FilterType, newRange: ClosedFloatingPointRange<Float>) {
        val snapped = newRange.snapToStep(type.fullRange)
        updateFilter(type) { current ->
            if (current.selectedRange != snapped) current.copy(selectedRange = snapped) else current
        }
    }

    fun applyPreset(preset: FilterPreset) {
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

    fun generateGames(quantity: Int) {
        if (quantity <= 0) return
        if (_generationState.value is GenerationUiState.Loading) return

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            try {
                generateGamesUseCase(quantity, _filterStates.value).collect { progress ->
                    handleProgress(progress)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _events.send(NavigationEvent.ShowSnackbar(R.string.game_generator_failure_generic))
                _generationState.value = GenerationUiState.Idle
            }
        }
    }

    private suspend fun handleProgress(progress: GenerationProgress) {
        when (val type = progress.progressType) {
            is GenerationProgressType.Started -> updateLoading(
                msg = R.string.general_loading,
                cur = 0,
                tot = progress.total
            )

            is GenerationProgressType.Step -> updateLoading(
                msg = when (type.step) {
                    GenerationStep.RANDOM_START -> R.string.game_generator_random_start
                    GenerationStep.HEURISTIC_START -> R.string.game_generator_heuristic_start
                    GenerationStep.RANDOM_FALLBACK -> R.string.game_generator_random_fallback
                },
                cur = progress.current,
                tot = progress.total
            )

            is GenerationProgressType.Attempt -> {
                val currentMsg = (_generationState.value as? GenerationUiState.Loading)?.messageRes
                    ?: R.string.general_loading
                updateLoading(msg = currentMsg, cur = progress.current, tot = progress.total)
            }

            is GenerationProgressType.Finished -> {
                saveGeneratedGamesUseCase(type.games)
                // expõe telemetria para UI/debug
                _generationTelemetry.value = type.telemetry
                _events.send(NavigationEvent.NavigateToGeneratedGames)
                _generationState.value = GenerationUiState.Idle
            }

            is GenerationProgressType.Failed -> {
                val messageRes = when (type.reason) {
                    GenerationFailureReason.NO_HISTORY -> R.string.game_generator_failure_no_history
                    GenerationFailureReason.FILTERS_TOO_STRICT -> R.string.game_generator_failure_filters_strict
                    GenerationFailureReason.GENERIC_ERROR -> R.string.game_generator_failure_generic
                }
                _events.send(NavigationEvent.ShowSnackbar(messageRes))
                _generationState.value = GenerationUiState.Idle
            }
        }
    }

    private fun updateFilter(type: FilterType, transform: (FilterState) -> FilterState) {
        _filterStates.update { list -> list.map { if (it.type == type) transform(it) else it } }
    }

    private fun updateLoading(@StringRes msg: Int, cur: Int, tot: Int) {
        _generationState.value = GenerationUiState.Loading(messageRes = msg, progress = cur, total = tot)
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        _generationState.value = GenerationUiState.Idle
    }

    fun requestResetFilters() {
        _showResetDialog.value = true
    }

    fun confirmResetFilters() {
        _filterStates.value = FilterType.entries.map { FilterState(type = it) }
        _showResetDialog.value = false
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun showFilterInfo(type: FilterType) {
        _filterInfoToShow.value = type
    }

    fun dismissFilterInfo() {
        _filterInfoToShow.value = null
    }
}

private fun ClosedFloatingPointRange<Float>.snapToStep(
    full: ClosedFloatingPointRange<Float>
): ClosedFloatingPointRange<Float> {
    val a = start.roundToInt().toFloat().coerceIn(full.start, full.endInclusive)
    val b = endInclusive.roundToInt().toFloat().coerceIn(full.start, full.endInclusive)

    val start = minOf(a, b)
    val end = maxOf(a, b)
    return start..end
}
