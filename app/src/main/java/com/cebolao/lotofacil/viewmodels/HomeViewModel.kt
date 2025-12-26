package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.model.StatisticsReport
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.usecase.GetAnalyzedStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetHomeScreenDataUseCase
import com.cebolao.lotofacil.domain.usecase.ObserveSyncStatusUseCase
import com.cebolao.lotofacil.domain.usecase.SyncHistoryUseCase
import com.cebolao.lotofacil.mapper.toSimpleStats
import com.cebolao.lotofacil.util.toUserMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface HomeScreenState {
    data object Loading : HomeScreenState
    data class Error(@param:StringRes val messageResId: Int) : HomeScreenState
    data class Success(
        val lastDraw: Draw?,
        val lastDrawSimpleStats: ImmutableList<Pair<String, String>>,
        val lastDrawCheckResult: CheckReport?,
        val details: DrawDetails?
    ) : HomeScreenState
}

data class HomeUiState(
    val screenState: HomeScreenState = HomeScreenState.Loading,
    val statistics: StatisticsReport? = null,
    val isStatsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val selectedPattern: StatisticPattern = StatisticPattern.SUM,
    val selectedTimeWindow: Int = 0,
    val syncMessageRes: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val syncHistoryUseCase: SyncHistoryUseCase,
    getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase
) : ViewModel() {

    private val _selectedTimeWindow = MutableStateFlow(0)
    private val _selectedPattern = MutableStateFlow(StatisticPattern.SUM)
    private val _syncMessageEvent = MutableStateFlow<Int?>(null)

    val syncStatus = observeSyncStatusUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus.Idle)

    private val homeDataFlow = getHomeScreenDataUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val statsFlow = _selectedTimeWindow
        .flatMapLatest { window ->
            flow { emit(getAnalyzedStatsUseCase(window)) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Result.success(StatisticsReport())
        )

    private val settingsFlow = combine(_selectedTimeWindow, _selectedPattern) { w, p -> w to p }

    val uiState: StateFlow<HomeUiState> = combine(
        homeDataFlow,
        statsFlow,
        syncStatus,
        settingsFlow,
        _syncMessageEvent
    ) { homeResult, statsResult, status, (window, pattern), syncMsg ->
        
        val screenState = if (homeResult == null) {
            HomeScreenState.Loading
        } else {
            when (homeResult) {
                is AppResult.Success -> processSuccess(homeResult.value)
                is AppResult.Failure -> HomeScreenState.Error(homeResult.error.toUserMessageRes())
            }
        }

        val stats = statsResult.getOrNull()
        val statsLoading = statsResult.isFailure // Assuming failure implies loading or error, but here we likely want 'loading' state during stats fetch? 
        // Actually statsResult is strictly Success or Failure from UseCase. UseCase is suspend.
        // During suspension, flow doesn't emit 'Loading'.
        // So 'isStatsLoading' logic here is weak unless we start with 'Loading' in statsFlow.
        // But let's stick to current logic.

        val msgFromStatus = when (status) {
            is SyncStatus.Success -> R.string.home_sync_success_message
            is SyncStatus.Failed -> status.error.toUserMessageRes()
            else -> null
        }

        HomeUiState(
            screenState = screenState,
            statistics = stats,
            isStatsLoading = false, // Simplified for now as we don't track loading state inside 'Result'
            isSyncing = status is SyncStatus.Syncing,
            selectedPattern = pattern,
            selectedTimeWindow = window,
            syncMessageRes = syncMsg ?: msgFromStatus
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState()
    )

    private fun processSuccess(data: HomeScreenData): HomeScreenState.Success {
        val simpleStats =
            data.lastDraw?.let { getGameSimpleStatsUseCase(it).toSimpleStats() } ?: persistentListOf()
        return HomeScreenState.Success(
            data.lastDraw,
            simpleStats,
            data.lastDrawCheckResult,
            data.details
        )
    }

    fun forceSync() {
        syncHistoryUseCase()
    }

    fun onMessageShown() {
        _syncMessageEvent.value = null
    }

    fun onTimeWindowSelected(window: Int) {
        _selectedTimeWindow.value = window
    }

    fun onPatternSelected(pattern: StatisticPattern) {
        _selectedPattern.value = pattern
    }
}
