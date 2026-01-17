package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.presentation.util.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data class Success(val draws: List<Draw>) : ResultsUiState
    data class Error(val messageRes: Int) : ResultsUiState
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : BaseViewModel() {

    val uiState: StateFlow<ResultsUiState> = historyRepository.observeHistory()
        .map { draws ->
            if (draws.isEmpty()) {
                ResultsUiState.Loading // Or Empty state, but Loading covers initial sync
            } else {
                ResultsUiState.Success(draws)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ResultsUiState.Loading
        )

    init {
        // Ensure sync triggers if needed
        launchCatching {
            historyRepository.syncHistoryIfNeeded()
        }
    }
}
