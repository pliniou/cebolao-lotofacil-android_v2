package com.cebolao.lotofacil.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Filters : AppRoute

    @Serializable
    data object GeneratedGames : AppRoute

    @Serializable
    data object Results : AppRoute

    @Serializable
    data object About : AppRoute

    @Serializable
    data class Checker(val numbers: List<Int> = emptyList()) : AppRoute
}
