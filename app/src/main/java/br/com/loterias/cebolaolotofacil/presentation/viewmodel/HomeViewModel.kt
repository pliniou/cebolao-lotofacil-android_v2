package br.com.loterias.cebolaolotofacil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import br.com.loterias.cebolaolotofacil.domain.usecase.GetRecentLotofacilResultsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val results: List<LotofacilResult> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val getRecentResults: GetRecentLotofacilResultsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchResults()
    }

    private fun fetchResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getRecentResults()
                .catch { e -> _uiState.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) } }
                .collect { data -> _uiState.update { it.copy(results = data, isLoading = false) } }
        }
    }
}
