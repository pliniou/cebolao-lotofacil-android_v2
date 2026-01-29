package br.com.loterias.cebolaolotofacil.presentation.viewmodel

import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import br.com.loterias.cebolaolotofacil.domain.usecase.GetRecentLotofacilResultsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var mockUseCase: GetRecentLotofacilResultsUseCase

    @Before
    fun setup() {
        mockUseCase = mockk()
        every { mockUseCase() } returns flowOf(emptyList())
    }

    @Test
    fun `viewModel initializes with empty state`() {
        viewModel = HomeViewModel(mockUseCase)
        assert(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun `selectResult updates selectedResult`() {
        viewModel = HomeViewModel(mockUseCase)
        val result = LotofacilResult(
            concurso = 2500,
            dezenas = listOf("01", "02"),
            data = "28/01/2025",
            valorAcumulado = 1000.0,
            valorPremioPrincipal = 500.0,
            ganhadores = 10,
            dataProximoSorteio = "01/02/2025",
            isFavorite = false
        )
        viewModel.selectResult(result)
        assert(viewModel.selectedResult.value == result)
    }

    @Test
    fun `clearError clears error message`() {
        viewModel = HomeViewModel(mockUseCase)
        viewModel.clearError()
        assert(viewModel.uiState.value.error == null)
    }
}
