package com.cebolao.lotofacil.navigation

import kotlinx.serialization.Serializable

@Serializable
object OnboardingRoute

@Serializable
object HomeRoute

@Serializable
object FiltersRoute

@Serializable
object GeneratedGamesRoute

@Serializable
object AboutRoute

@Serializable
data class CheckerRoute(
    val numbers: List<Int> = emptyList()
)
