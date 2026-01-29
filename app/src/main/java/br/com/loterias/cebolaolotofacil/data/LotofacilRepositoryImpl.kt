package br.com.loterias.cebolaolotofacil.data

import br.com.loterias.cebolaolotofacil.data.local.db.LotofacilResultDao
import br.com.loterias.cebolaolotofacil.data.remote.LotofacilApiService
import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult
import br.com.loterias.cebolaolotofacil.domain.repository.LotofacilRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Implementation of LotofacilRepository with network and local caching
 * Follows the Repository pattern with offline-first strategy
 */
class LotofacilRepositoryImpl(
    private val apiService: LotofacilApiService,
    private val localDao: LotofacilResultDao
) : LotofacilRepository {

    /**
     * Get recent results with automatic caching
     * Tries local cache first, then falls back to API with retry logic
     */
    override fun getRecentResults(): Flow<List<LotofacilResult>> = flow {
        var hasCachedData = false
        
        try {
            // Try to fetch fresh data from API first
            val freshResults = apiService.getRecentResults(limit = 10)
            val entity = freshResults.toEntity()
            localDao.insertResult(entity)
            emit(listOf(entity.toDomain()))
            hasCachedData = true
        } catch (apiException: Exception) {
            Timber.w(apiException, "Failed to fetch from API, attempting to use cached data")
        }

        // Emit cached data if API failed
        if (!hasCachedData) {
            try {
                localDao.getRecentResults(limit = 10).collect { cachedResults ->
                    if (cachedResults.isNotEmpty()) {
                        emit(cachedResults.map { it.toDomain() })
                    } else {
                        throw Exception("No cached data and API fetch failed")
                    }
                }
            } catch (exception: Exception) {
                Timber.e(exception, "Error fetching recent results")
                throw exception
            }
        }
    }

    /**
     * Get result by specific concurso number
     */
    override suspend fun getResultByConcurso(concurso: Int): LotofacilResult? {
        return try {
            // Check local cache first
            val cached = localDao.getResultByConcurso(concurso)
            if (cached != null) {
                return cached.toDomain()
            }

            // If not in cache, fetch from API
            val result = apiService.getResultByConcurso(concurso)
            val domainResult = result.toDomain()
            localDao.insertResult(domainResult.toEntity())
            domainResult
        } catch (exception: Exception) {
            Timber.e(exception, "Error fetching result for concurso: $concurso")
            null
        }
    }

    /**
     * Get paginated results for infinite scroll
     */
    override fun getResultsPaginated(limit: Int, offset: Int): Flow<List<LotofacilResult>> =
        localDao.getResultsPaginated(limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Get favorite results
     */
    override fun getFavoriteResults(): Flow<List<LotofacilResult>> =
        localDao.getFavoriteResults().map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Toggle favorite status
     */
    override suspend fun toggleFavorite(concurso: Int) {
        localDao.toggleFavorite(concurso)
    }

    /**
     * Search results by number
     */
    override fun searchByNumber(number: String): Flow<List<LotofacilResult>> =
        localDao.searchByNumber(number).map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Get results count for UI indicators
     */
    override fun getResultsCount(): Flow<Int> = localDao.getResultCount()

    /**
     * Clear local cache
     */
    override suspend fun clearCache() {
        localDao.deleteAllResults()
    }
}
