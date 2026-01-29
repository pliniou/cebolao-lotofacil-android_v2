package br.com.loterias.cebolaolotofacil.data.remote

import br.com.loterias.cebolaolotofacil.data.remote.dto.LotofacilResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for Lotofácil API
 * Provides endpoints to fetch lottery results from the Brazilian Caixa API
 */
interface LotofacilApiService {

    /**
     * Fetch recent Lotofácil results
     * @param limit Maximum number of results to return (default 10)
     */
    @GET("loterias/v2/lotofacil")
    suspend fun getRecentResults(
        @Query("limit") limit: Int = 10
    ): LotofacilResultDto

    /**
     * Fetch a specific Lotofácil result by draw number (Concurso)
     * @param concurso Draw number
     */
    @GET("loterias/v2/lotofacil")
    suspend fun getResultByConcurso(
        @Query("concurso") concurso: Int
    ): LotofacilResultDto

    companion object {
        // Official Caixa API endpoint
        const val BASE_URL = "https://www.loteriascaixa.gov.br/wps/portal/!ut/p/a1/"
    }
}
