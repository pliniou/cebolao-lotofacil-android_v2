package br.com.loterias.cebolaolotofacil.domain.repository

import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Lotofácil data access
 * Abstracts data source (API/local cache) from domain logic
 */
interface LotofacilRepository {

    /**
     * Get recent Lotofácil results
     */
    fun getRecentResults(): Flow<List<LotofacilResult>>

    /**
     * Get result by specific concurso number
     */
    suspend fun getResultByConcurso(concurso: Int): LotofacilResult?

    /**
     * Get paginated results for infinite scroll
     */
    fun getResultsPaginated(limit: Int, offset: Int): Flow<List<LotofacilResult>>

    /**
     * Get results marked as favorite
     */
    fun getFavoriteResults(): Flow<List<LotofacilResult>>

    /**
     * Toggle favorite status for a result
     */
    suspend fun toggleFavorite(concurso: Int)

    /**
     * Search results by number
     */
    fun searchByNumber(number: String): Flow<List<LotofacilResult>>

    /**
     * Get total count of cached results
     */
    fun getResultsCount(): Flow<Int>

    /**
     * Clear local cache
     */
    suspend fun clearCache()
}
