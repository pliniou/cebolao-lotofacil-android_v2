package br.com.loterias.cebolaolotofacil.data

import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import br.com.loterias.cebolaolotofacil.domain.repository.LotofacilRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

class LotofacilRepositoryImpl : LotofacilRepository {
    override fun getRecentResults(): Flow<List<LotofacilResult>> = flow {
        delay(500) // Simulate fetch
        emit(
            listOf(
                LotofacilResult(
                    concurso = 3041,
                    dezenas = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15"),
                    data = "2024-01-15"
                )
            )
        )
    }
}
