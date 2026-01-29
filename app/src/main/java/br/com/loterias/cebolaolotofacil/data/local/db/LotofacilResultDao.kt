package br.com.loterias.cebolaolotofacil.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.loterias.cebolaolotofacil.data.local.entity.LotofacilResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Lotofácil result database operations
 */
@Dao
interface LotofacilResultDao {

    /**
     * Get all results as a Flow for reactive updates
     */
    @Query("SELECT * FROM lotofacil_results ORDER BY concurso DESC")
    fun getAllResults(): Flow<List<LotofacilResultEntity>>

    /**
     * Get results paginated
     */
    @Query("""
        SELECT * FROM lotofacil_results 
        ORDER BY concurso DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getResultsPaginated(limit: Int, offset: Int): Flow<List<LotofacilResultEntity>>

    /**
     * Get a specific result by concurso number
     */
    @Query("SELECT * FROM lotofacil_results WHERE concurso = :concurso")
    suspend fun getResultByConcurso(concurso: Int): LotofacilResultEntity?

    /**
     * Get recent results (last N)
     */
    @Query("""
        SELECT * FROM lotofacil_results 
        ORDER BY concurso DESC 
        LIMIT :limit
    """)
    fun getRecentResults(limit: Int = 10): Flow<List<LotofacilResultEntity>>

    /**
     * Get favorite results
     */
    @Query("""
        SELECT * FROM lotofacil_results 
        WHERE is_favorite = 1 
        ORDER BY concurso DESC
    """)
    fun getFavoriteResults(): Flow<List<LotofacilResultEntity>>

    /**
     * Check if result exists
     */
    @Query("SELECT COUNT(*) > 0 FROM lotofacil_results WHERE concurso = :concurso")
    suspend fun resultExists(concurso: Int): Boolean

    /**
     * Insert a single result
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: LotofacilResultEntity)

    /**
     * Insert multiple results
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<LotofacilResultEntity>)

    /**
     * Update a result
     */
    @Update
    suspend fun updateResult(result: LotofacilResultEntity)

    /**
     * Toggle favorite status
     */
    @Query("UPDATE lotofacil_results SET is_favorite = NOT is_favorite WHERE concurso = :concurso")
    suspend fun toggleFavorite(concurso: Int)

    /**
     * Delete a specific result
     */
    @Delete
    suspend fun deleteResult(result: LotofacilResultEntity)

    /**
     * Delete all results older than timestamp
     */
    @Query("DELETE FROM lotofacil_results WHERE sync_timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    /**
     * Delete all results
     */
    @Query("DELETE FROM lotofacil_results")
    suspend fun deleteAllResults()

    /**
     * Get total count of results
     */
    @Query("SELECT COUNT(*) FROM lotofacil_results")
    fun getResultCount(): Flow<Int>

    /**
     * Get results by search query on numbers (dezenas)
     */
    @Query("""
        SELECT * FROM lotofacil_results 
        WHERE dezenas LIKE '%' || :number || '%' 
        ORDER BY concurso DESC
    """)
    fun searchByNumber(number: String): Flow<List<LotofacilResultEntity>>

    /**
     * Get results by date range
     */
    @Query("""
        SELECT * FROM lotofacil_results 
        WHERE data BETWEEN :startDate AND :endDate 
        ORDER BY concurso DESC
    """)
    fun getResultsByDateRange(startDate: String, endDate: String): Flow<List<LotofacilResultEntity>>
}
