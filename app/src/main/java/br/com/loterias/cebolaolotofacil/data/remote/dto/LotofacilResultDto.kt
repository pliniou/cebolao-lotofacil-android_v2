package br.com.loterias.cebolaolotofacil.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for Lotofácil results from API
 */
@JsonClass(generateAdapter = true)
data class LotofacilResultDto(
    @Json(name = "concurso")
    val concurso: Int,

    @Json(name = "dezenas")
    val dezenas: List<String>,

    @Json(name = "data")
    val data: String,

    @Json(name = "valor_acumulado")
    val valorAcumulado: Double? = null,

    @Json(name = "valor_premio_principal")
    val valorPremioPrincipal: Double? = null,

    @Json(name = "ganhadores_principais")
    val ganhadores: Int? = null,

    @Json(name = "data_proximo_sorteio")
    val dataProximoSorteio: String? = null
)
