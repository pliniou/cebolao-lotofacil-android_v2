package com.cebolao.lotofacil.domain.model

/** Métricas completas calculadas para um jogo (para uso nos filtros) */
data class GameComputedMetrics(
    val sum: Int,
    val evens: Int,
    val primes: Int,
    val fibonacci: Int,
    val frame: Int,
    val sequences: Int,
    val multiplesOf3: Int,
    val center: Int,
    val repeated: Int
)
