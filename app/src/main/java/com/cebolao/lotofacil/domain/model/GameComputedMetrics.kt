package com.cebolao.lotofacil.domain.model

/** Métricas completas calculadas para um jogo (para uso nos filtros) */
data class GameComputedMetrics(
    val sum: Int,
    val evens: Int,
    val primes: Int,
    val fibonacci: Int,
    val frame: Int,

    val sequences: Int,
    val lines: Int,
    val columns: Int,
    val quadrants: Int,
    val repeated: Int
)
