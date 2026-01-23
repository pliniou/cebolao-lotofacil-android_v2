package com.cebolao.lotofacil.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlin.reflect.KClass

// Minimal typed navigation compatibility helpers used by the app.

/**
 * Returns a stable route *name* for an arbitrary route object.
 * For simple object-based routes we use a short, deterministic name.
 * For `CheckerRoute` we optionally encode numbers as a CSV query parameter.
 */
fun routeNameFor(obj: Any): String = when (obj) {
    is CheckerRoute -> if (obj.numbers.isEmpty()) "checker" else "checker?numbers=${obj.numbers.joinToString(",")}" 
    is HomeRoute -> "home"
    is OnboardingRoute -> "onboarding"
    is FiltersRoute -> "filters"
    is GeneratedGamesRoute -> "generatedgames"
    is ResultsRoute -> "results"
    is AboutRoute -> "about"
    else -> obj::class.simpleName?.lowercase() ?: obj.toString()
}

inline fun <reified T : Any> routePatternFor(): String = when (T::class) {
    CheckerRoute::class -> "checker?numbers={numbers}"
    OnboardingRoute::class -> "onboarding"
    HomeRoute::class -> "home"
    FiltersRoute::class -> "filters"
    GeneratedGamesRoute::class -> "generatedgames"
    ResultsRoute::class -> "results"
    AboutRoute::class -> "about"
    else -> T::class.simpleName?.lowercase() ?: T::class.qualifiedName!!
}

inline fun <reified T : Any> routeName(): String = when (T::class) {
    CheckerRoute::class -> "checker"
    OnboardingRoute::class -> "onboarding"
    HomeRoute::class -> "home"
    FiltersRoute::class -> "filters"
    GeneratedGamesRoute::class -> "generatedgames"
    ResultsRoute::class -> "results"
    AboutRoute::class -> "about"
    else -> T::class.simpleName?.lowercase() ?: T::class.qualifiedName!!
}

/**
 * Convenience overload so callers can continue using `navController.navigate(HomeRoute)`.
 */
fun NavController.navigate(routeObj: Any, builder: NavOptionsBuilder.() -> Unit = {}) {
    this.navigate(routeNameFor(routeObj), builder)
}


/**
 * NavDestination helper `hasRoute(kClass)` used by the bottom bar.
 */
fun NavDestination.hasRoute(routeClass: KClass<*>): Boolean {
    val name = routeClass.simpleName?.lowercase() ?: return false
    return this.route?.startsWith(name) ?: false
}

/**
 * Compatibility `hierarchy` property used across the project.
 */
val NavDestination.hierarchy: Sequence<NavDestination>
    get() = generateSequence(this) { it.parent }

/**
 * SavedStateHandle helper that reconstructs typed route objects for small set of routes.
 * This intentionally keeps logic local and tolerant: missing or malformed args throw
 * IllegalArgumentException which callers already handle.
 */
inline fun <reified T : Any> SavedStateHandle.toRoute(): T {
    return when (T::class) {
        CheckerRoute::class -> {
            val nums = (get<String>("numbers") ?: get<String>("route")?.substringAfter('?')?.substringAfter("numbers=") ?: "").takeIf { it.isNotBlank() }
            val list = nums?.split(',')?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            CheckerRoute(list) as T
        }
        OnboardingRoute::class -> OnboardingRoute as T
        HomeRoute::class -> HomeRoute as T
        FiltersRoute::class -> FiltersRoute as T
        GeneratedGamesRoute::class -> GeneratedGamesRoute as T
        ResultsRoute::class -> ResultsRoute as T
        AboutRoute::class -> AboutRoute as T
        else -> throw IllegalArgumentException("Unsupported route type: ${T::class}")
    }
}

/**
 * Returns Nav arguments for the `CheckerRoute` composable.
 */
fun checkerNavArgs() = listOf(
    navArgument("numbers") { type = NavType.StringType; defaultValue = "" }
)
