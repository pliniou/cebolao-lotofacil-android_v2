package br.com.loterias.cebolaolotofacil.domain.usecase

import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import br.com.loterias.cebolaolotofacil.domain.repository.LotofacilRepository
import kotlinx.coroutines.flow.Flow

class GetRecentLotofacilResultsUseCase(
    private val repository: LotofacilRepository
) {
    operator fun invoke(): Flow<List<LotofacilResult>> = repository.getRecentResults()
}
