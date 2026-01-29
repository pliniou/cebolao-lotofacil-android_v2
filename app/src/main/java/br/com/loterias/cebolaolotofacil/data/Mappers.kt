package br.com.loterias.cebolaolotofacil.data

import br.com.loterias.cebolaolotofacil.data.local.entity.LotofacilResultEntity
import br.com.loterias.cebolaolotofacil.data.remote.dto.LotofacilResultDto
import br.com.loterias.cebolaolotofacil.domain.model.LotofacilResult

/**
 * Extension functions for mapping between data layers
 */

// Dto to Domain
fun LotofacilResultDto.toDomain(): LotofacilResult = LotofacilResult(
    concurso = concurso,
    dezenas = dezenas,
    data = data,
    valorAcumulado = valorAcumulado,
    valorPremioPrincipal = valorPremioPrincipal,
    ganhadores = ganhadores,
    dataProximoSorteio = dataProximoSorteio
)

// Dto to Entity
fun LotofacilResultDto.toEntity(): LotofacilResultEntity = LotofacilResultEntity(
    concurso = concurso,
    dezenas = dezenas.joinToString(","),
    data = data,
    valorAcumulado = valorAcumulado,
    valorPremioPrincipal = valorPremioPrincipal,
    ganhadores = ganhadores,
    dataProximoSorteio = dataProximoSorteio
)

// Domain to Entity
fun LotofacilResult.toEntity(): LotofacilResultEntity = LotofacilResultEntity(
    concurso = concurso,
    dezenas = dezenas.joinToString(","),
    data = data,
    valorAcumulado = valorAcumulado,
    valorPremioPrincipal = valorPremioPrincipal,
    ganhadores = ganhadores,
    dataProximoSorteio = dataProximoSorteio
)

// Entity to Domain
fun LotofacilResultEntity.toDomain(): LotofacilResult = LotofacilResult(
    concurso = concurso,
    dezenas = dezenas.split(",").map { it.trim() },
    data = data,
    valorAcumulado = valorAcumulado,
    valorPremioPrincipal = valorPremioPrincipal,
    ganhadores = ganhadores,
    dataProximoSorteio = dataProximoSorteio,
    isFavorite = isFavorite
)
