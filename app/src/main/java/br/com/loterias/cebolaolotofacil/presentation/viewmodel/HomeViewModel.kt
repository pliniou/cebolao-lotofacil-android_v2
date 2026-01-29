package br.com.loterias.cebolaolotofacil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import br.com.loterias.cebolaolotofacil.domain.usecase.GetRecentLotofacilResultsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * UI State for Home screen
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val results: List<LotofacilResult> = emptyList(),
    val error: String? = null,
    val isEmpty: Boolean = false,
    val resultsCount: Int = 0,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 0
)

/**
 * ViewModel for Home screen
 * Manages UI state, error handling, pagination, and user interactions
 */
class HomeViewModel(
    private val getRecentResults: GetRecentLotofacilResultsUseCase
) : ViewModel() {

    companion object {
        private const val ITEMS_PER_PAGE = 20
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedResult = MutableStateFlow<LotofacilResult?>(null)
    val selectedResult: StateFlow<LotofacilResult?> = _selectedResult.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        viewModelScope.launch { fetchResults() }
    }

    /**
     * Fetch recent lottery results with proper error handling
     */
    private suspend fun fetchResults() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        getRecentResults()
            .catch { e ->
                Timber.e(e, "Error fetching results")
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Erro ao carregar resultados",
                        isLoading = false,
                        isEmpty = true
                    )
                }
            }
            .collect { data ->
                _uiState.update { state ->
                    state.copy(
                        results = data,
                        isLoading = false,
                        isEmpty = data.isEmpty(),
                        resultsCount = data.size,
                        error = null
                    )
                }
            }
    }

    /**
     * Refresh results (pull-to-refresh)
     */
    fun refreshResults() {
        viewModelScope.launch {
            _refreshing.value = true
            try {
                fetchResults()
            } finally {
                _refreshing.value = false
            }
        }
    }

    /**
     * Retry loading after error
     */
    fun retryLoading() {
        Timber.d("Retrying to load results")
        viewModelScope.launch { fetchResults() }
    }

    /**
     * Select a result for details view
     */
    fun selectResult(result: LotofacilResult) {
        _selectedResult.value = result
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Load more items for pagination (infinite scroll)
     */
    fun loadMore() {
        val currentState = _uiState.value
        
        // Prevent loading if already loading or no more items
        if (currentState.isLoadingMore || !currentState.canLoadMore) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            try {
                val nextPage = currentState.currentPage + 1

                // Consome apenas um "snapshot" para evitar coleções infinitas/duplicações.
                val newData = getRecentResults()
                    .catch { e ->
                        Timber.e(e, "Error loading more results")
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                error = "Erro ao carregar mais resultados"
                            )
                        }
                    }
                    .firstOrNull()
                    .orEmpty()

                _uiState.update { state ->
                    state.copy(
                        results = state.results + newData,
                        isLoadingMore = false,
                        currentPage = nextPage,
                        canLoadMore = newData.size == ITEMS_PER_PAGE,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error in loadMore")
                _uiState.update { 
                    it.copy(
                        isLoadingMore = false,
                        error = "Erro ao carregar mais"
                    ) 
                }
            }
        }
    }
}
