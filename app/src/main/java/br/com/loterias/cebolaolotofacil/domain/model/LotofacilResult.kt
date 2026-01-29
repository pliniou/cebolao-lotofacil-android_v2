package br.com.loterias.cebolaolotofacil.domain.model

/**
 * Domain model for Lotofácil lottery result
 */
data class LotofacilResult(
    val concurso: Int,
    val dezenas: List<String>,
    val data: String,
    val valorAcumulado: Double? = null,
    val valorPremioPrincipal: Double? = null,
    val ganhadores: Int? = null,
    val dataProximoSorteio: String? = null,
    val isFavorite: Boolean = false
)
