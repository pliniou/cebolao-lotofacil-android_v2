package com.cebolao.lotofacil.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.usecase.GetAnalyzedStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetHomeScreenDataUseCase
import com.cebolao.lotofacil.domain.usecase.ObserveSyncStatusUseCase
import com.cebolao.lotofacil.domain.usecase.SyncHistoryUseCase
import com.cebolao.lotofacil.mapper.toSimpleStats
import com.cebolao.lotofacil.ui.model.UiDraw
import com.cebolao.lotofacil.ui.model.UiDrawDetails
import com.cebolao.lotofacil.ui.model.UiStatisticsReport
import com.cebolao.lotofacil.ui.model.toUiModel
import com.cebolao.lotofacil.util.Formatters
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.util.toAppError
import com.cebolao.lotofacil.util.toUserMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.cebolao.lotofacil.presentation.util.Async

/**
 * Represents the different states of the home screen.
 */
sealed interface HomeScreenState {
    /** Initial loading state. */
    data object Loading : HomeScreenState

    /** Error state with a localized message resource. */
    data class Error(@param:StringRes val messageResId: Int) : HomeScreenState

    /**
     * Success state containing all home screen data.
     */
    data class Success(
        val lastDraw: UiDraw?,
        val lastDrawSimpleStats: ImmutableList<Pair<String, String>>,
        val lastDrawCheckResult: CheckReport?,
        val details: UiDrawDetails?,
        val nextDrawInfo: NextDrawInfo?
    ) : HomeScreenState
}

/**
 * UI state for the Home screen.
 */
@androidx.compose.runtime.Immutable
data class HomeUiState(
    val screenState: HomeScreenState = HomeScreenState.Loading,
    val statistics: UiStatisticsReport? = null,
    val isStatsLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val selectedPattern: StatisticPattern = StatisticPattern.SUM,
    val selectedTimeWindow: Int = 0,
    val syncMessageRes: Int? = null
)

/**
 * ViewModel for the Home screen.
 * Manages home screen data, statistics, and synchronization status.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val syncHistoryUseCase: SyncHistoryUseCase,
    getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase
) : BaseViewModel() {

    private val _selectedTimeWindow = MutableStateFlow(0)
    private val _selectedPattern = MutableStateFlow(StatisticPattern.SUM)
    private val _syncMessageEvent = MutableStateFlow<Int?>(null)

    val syncStatus: StateFlow<SyncStatus> = observeSyncStatusUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), SyncStatus.Idle)

    private val homeDataFlow: StateFlow<AppResult<HomeScreenData>?> = getHomeScreenDataUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val statsFlow = _selectedTimeWindow.flatMapLatest { window ->
        flow {
            emit(Async.Loading)
            val result = getAnalyzedStatsUseCase(window)
            result.fold(
                onSuccess = { emit(Async.Success(it.toUiModel())) },
                onFailure = { 
                    val error = it.toAppError()
                    emit(Async.Error(error.toUserMessageRes())) 
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), Async.Loading)

    // Combine first 3 flows
    private val combinedData = combine(
        homeDataFlow,
        syncStatus,
        statsFlow
    ) { homeResult, status, statsState ->
        Triple(homeResult, status, statsState)
    }

    init {
        viewModelScope.launch {
            syncStatus
                .map { status ->
                    when (status) {
                        is SyncStatus.Success -> R.string.home_sync_success_message
                        is SyncStatus.Failed -> status.error.toUserMessageRes()
                        else -> null
                    }
                }
                .distinctUntilChanged()
                .collect { _syncMessageEvent.value = it }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        combinedData,
        _selectedTimeWindow,
        _selectedPattern,
        _syncMessageEvent
    ) { (homeResult, status, statsState), window, pattern, syncMsg ->

        val screenState = when (homeResult) {
            null -> HomeScreenState.Loading
            is AppResult.Success -> processSuccess(homeResult.value)
            is AppResult.Failure -> HomeScreenState.Error(homeResult.error.toUserMessageRes())
        }

        val (statistics, isStatsLoading) = when (statsState) {
            is Async.Success -> statsState.data to false
            is Async.Loading -> null to true
            is Async.Error -> null to false
            Async.Uninitialized -> null to false
        }

        HomeUiState(
            screenState = screenState,
            statistics = statistics,
            isStatsLoading = isStatsLoading,
            isSyncing = status is SyncStatus.Syncing,
            selectedPattern = pattern,
            selectedTimeWindow = window,
            syncMessageRes = syncMsg
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        HomeUiState()
    )

    /**
     * Single entry point for UI events.
     */
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.ForceSync -> forceSync()
            is HomeUiEvent.OnMessageShown -> onMessageShown()
            is HomeUiEvent.OnTimeWindowSelected -> onTimeWindowSelected(event.window)
            is HomeUiEvent.OnPatternSelected -> onPatternSelected(event.pattern)
        }
    }

    /**
     * Processes successful home data into UI state.
     */
    private fun processSuccess(data: HomeScreenData): HomeScreenState.Success {
        val simpleStats =
            data.lastDraw?.let { getGameSimpleStatsUseCase(it).toSimpleStats() } 
                ?: persistentListOf()

        return HomeScreenState.Success(
            data.lastDraw?.toUiModel(),
            simpleStats,
            data.lastDrawCheckResult,
            data.details?.toUiModel(),
            mapNextDrawInfo(data.details)
        )
    }

    private fun mapNextDrawInfo(details: com.cebolao.lotofacil.domain.model.DrawDetails?): NextDrawInfo? {
        if (details == null) return null
        
        val contestNumber = details.nextContestNumber
        val contestDate = details.nextContestDate

        return if (contestNumber != null && contestNumber > 0 && !contestDate.isNullOrBlank()) {
            NextDrawInfo(
                contestNumber = contestNumber,
                formattedDate = contestDate,
                formattedPrize = Formatters.formatCurrency(details.nextEstimatedPrize),
                formattedPrizeFinalFive = Formatters.formatCurrency(details.accumulatedValue05)
            )
        } else {
            null
        }
    }

    /**
     * Triggers a manual synchronization of lottery history.
     */
    private fun forceSync() {
        viewModelScope.launch {
            when (syncHistoryUseCase()) {
                is AppResult.Success -> {
                    // Sync success handled by repository state flow
                }
                is AppResult.Failure -> {
                    // Sync failure also reflected in repository state, but we could show snackbar here if needed
                }
            }
        }
    }

    /**
     * Clears the current sync message after it has been shown to the user.
     */
    private fun onMessageShown() {
        _syncMessageEvent.value = null
    }

    /**
     * Updates the selected time window for statistics.
     */
    private fun onTimeWindowSelected(window: Int) {
        _selectedTimeWindow.value = window
    }

    /**
     * Updates the selected pattern for statistics visualization.
     */
    private fun onPatternSelected(pattern: StatisticPattern) {
        _selectedPattern.value = pattern
    }
}

/**
 * UI Events for HomeViewModel.
 */
sealed interface HomeUiEvent {
    data object ForceSync : HomeUiEvent
    data object OnMessageShown : HomeUiEvent
    data class OnTimeWindowSelected(val window: Int) : HomeUiEvent
    data class OnPatternSelected(val pattern: StatisticPattern) : HomeUiEvent
}
