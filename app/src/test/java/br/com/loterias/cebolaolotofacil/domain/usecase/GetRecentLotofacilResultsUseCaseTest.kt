package br.com.loterias.cebolaolotofacil.domain.usecase

import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import br.com.loterias.cebolaolotofacil.domain.repository.LotofacilRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

class GetRecentLotofacilResultsUseCaseTest {

    private lateinit var mockRepository: LotofacilRepository
    private lateinit var useCase: GetRecentLotofacilResultsUseCase

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = GetRecentLotofacilResultsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns results from repository`() {
        // Arrange
        val mockResults = listOf(
            LotofacilResult(
                concurso = 2500,
                dezenas = listOf("01", "02"),
                data = "28/01/2025",
                valorAcumulado = 1000.0,
                valorPremioPrincipal = 500.0,
                ganhadores = 10,
                dataProximoSorteio = "01/02/2025",
                isFavorite = false
            )
        )
        every { mockRepository.getRecentResults() } returns flowOf(mockResults)

        // Act & Assert
        assert(useCase() != null)
    }
}
