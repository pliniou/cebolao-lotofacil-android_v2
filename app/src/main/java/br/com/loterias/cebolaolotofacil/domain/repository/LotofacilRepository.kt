package br.com.loterias.cebolaolotofacil.domain.repository

import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import kotlinx.coroutines.flow.Flow

interface LotofacilRepository {
    fun getRecentResults(): Flow<List<LotofacilResult>>
}
