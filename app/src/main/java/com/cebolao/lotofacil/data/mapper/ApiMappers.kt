package com.cebolao.lotofacil.data.mapper

import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.data.network.MunicipioUFGanhadores
import com.cebolao.lotofacil.data.network.RateioPremio
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.model.PrizeRate
import com.cebolao.lotofacil.domain.model.WinnerLocation
import java.text.SimpleDateFormat
import java.util.Locale

fun LotofacilApiResult.toDraw(): Draw {
    val numbers = listaDezenas.mapNotNull { it.toIntOrNull() }.toSet()
    val dateMillis = dataApuracao.parseDateToMillis()
    
    return Draw.fromNumbers(contestNumber = numero, numbers = numbers, date = dateMillis)
}

fun LotofacilApiResult.toDrawDetails(): DrawDetails {
    val draw = this.toDraw()
    val nextContestNumber = draw.contestNumber + 1
    
    return DrawDetails(
        draw = draw,
        nextEstimatedPrize = valorEstimadoProximoConcurso,
        nextContestDate = dataProximoConcurso,
        nextContestNumber = nextContestNumber,
        accumulatedValue05 = valorAcumuladoConcurso05,
        accumulatedValueSpecial = valorAcumuladoConcursoEspecial,
        location = if (localSorteio != null) "$nomeMunicipioUFSorteio - $localSorteio" else nomeMunicipioUFSorteio,
        winnersByState = listaMunicipioUFGanhadores.map { it.toWinnerLocation() },
        prizeRates = listaRateioPremio.map { it.toPrizeRate() }
    )
}

fun MunicipioUFGanhadores.toWinnerLocation(): WinnerLocation {
    return WinnerLocation(
        state = uf,
        city = municipio,
        count = ganhadores
    )
}

fun RateioPremio.toPrizeRate(): PrizeRate {
    return PrizeRate(
        description = descricaoFaixa,
        winnerCount = numeroDeGanhadores,
        prizeValue = valorPremio
    )
}

private fun String?.parseDateToMillis(): Long? {
    if (this == null) return null
    return try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        format.parse(this)?.time
    } catch (_: Exception) {
        null
    }
}
