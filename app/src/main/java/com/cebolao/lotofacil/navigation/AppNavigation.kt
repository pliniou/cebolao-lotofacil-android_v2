package com.cebolao.lotofacil.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.util.CHECKER_ARG_SEPARATOR

@Stable
sealed class Screen(
    val route: String,
    @get:StringRes val titleRes: Int? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    val baseRoute: String get() = route.substringBefore('?')
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home", R.string.nav_home, AppIcons.Home, AppIcons.HomeOutlined)
    data object Filters : Screen("filters", R.string.nav_filters, AppIcons.Tune, AppIcons.TuneOutlined)
    data object GeneratedGames : Screen("generated_games", R.string.nav_games, AppIcons.List, AppIcons.ListOutlined)
    data object About : Screen("about", R.string.nav_about, AppIcons.Info, AppIcons.InfoOutlined)

    data object Checker : Screen(
        route = "checker?numbers={numbers}",
        titleRes = R.string.nav_checker,
        selectedIcon = AppIcons.Analytics,
        unselectedIcon = AppIcons.AnalyticsOutlined
    ) {
        const val ARG_NUMBERS = "numbers"
        val arguments = listOf(navArgument(ARG_NUMBERS) { type = NavType.StringType; nullable = true; defaultValue = null })
        fun createRoute(numbers: Set<Int>) = "checker?$ARG_NUMBERS=${numbers.joinToString(CHECKER_ARG_SEPARATOR.toString())}"
    }
}

val bottomNavItems = listOf(Screen.Home, Screen.Filters, Screen.GeneratedGames, Screen.Checker, Screen.About)

fun NavController.navigateToChecker(numbers: Set<Int>) {
    val route = Screen.Checker.createRoute(numbers)
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
