package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.presentation.util.UiState
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.util.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ResultsUiState : UiState {
    data object Loading : ResultsUiState
    data class Success(val draws: List<Draw>) : ResultsUiState
    data class Error(val messageRes: Int) : ResultsUiState
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : BaseViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val syncMessage = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ResultsUiState> = combine(
        historyRepository.observeHistory(),
        syncMessage
    ) { draws, errorRes ->
        when {
            draws.isNotEmpty() -> ResultsUiState.Success(draws)
            errorRes != null -> ResultsUiState.Error(errorRes)
            else -> ResultsUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        initialValue = ResultsUiState.Loading
    )

    init {
        refreshHistory()
    }

    fun refreshHistory() {
        viewModelScope.launchCatching {
            _isRefreshing.value = true
            when (historyRepository.syncHistoryIfNeeded()) {
                is AppResult.Success -> {
                    syncMessage.value = null
                }
                is AppResult.Failure -> {
                    syncMessage.value = com.cebolao.lotofacil.R.string.results_error_message
                }
            }
            _isRefreshing.value = false
        }
    }
}
