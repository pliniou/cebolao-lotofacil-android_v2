package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.local.db.DrawDetailsEntity
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.model.PrizeRate
import com.cebolao.lotofacil.domain.model.WinnerLocation

fun LotofacilApiResult.toDrawDetailsEntity(): DrawDetailsEntity {
    return DrawDetailsEntity(
        contestNumber = numero,
        nextEstimatedPrize = valorEstimadoProximoConcurso,
        nextContestDate = dataProximoConcurso,
        nextContestNumber = numero + 1,
        accumulatedValue05 = valorAcumuladoConcurso05,
        accumulatedValueSpecial = valorAcumuladoConcursoEspecial,
        location = "$localSorteio - $nomeMunicipioUFSorteio",
        winnersByState = listaMunicipioUFGanhadores.map { 
            WinnerLocation(it.uf, it.municipio, it.ganhadores) 
        },
        prizeRates = listaRateioPremio.map { 
            PrizeRate(it.descricaoFaixa, it.numeroDeGanhadores, it.valorPremio) 
        }
    )
}

fun DrawDetailsEntity.toDrawDetails(draw: Draw): DrawDetails {
    return DrawDetails(
        draw = draw,
        nextEstimatedPrize = nextEstimatedPrize,
        nextContestDate = nextContestDate,
        nextContestNumber = nextContestNumber,
        accumulatedValue05 = accumulatedValue05,
        accumulatedValueSpecial = accumulatedValueSpecial,
        location = location,
        winnersByState = winnersByState,
        prizeRates = prizeRates
    )
}
