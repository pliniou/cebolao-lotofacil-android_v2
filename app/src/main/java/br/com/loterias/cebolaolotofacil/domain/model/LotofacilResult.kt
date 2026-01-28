package br.com.loterias.cebolaolotofacil.domain.model

data class LotofacilResult(
    val concurso: Int,
    val dezenas: List<String>,
    val data: String
)
