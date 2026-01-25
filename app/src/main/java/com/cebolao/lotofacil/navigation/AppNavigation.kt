package com.cebolao.lotofacil.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppIcons

/**
 * Metadata wrapper for Navigation Destinations, primarily for the Bottom Navigation Bar.
 */
@Stable
sealed class Screen(
    val route: AppRoute,
    @get:StringRes val titleRes: Int? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Home : Screen(
        AppRoute.Home,
        R.string.nav_home,
        AppIcons.Home,
        AppIcons.HomeOutlined
    )

    data object Filters : Screen(
        AppRoute.Filters,
        R.string.nav_filters,
        AppIcons.Tune,
        AppIcons.TuneOutlined
    )

    data object GeneratedGames : Screen(
        AppRoute.GeneratedGames,
        R.string.nav_games,
        AppIcons.List,
        AppIcons.ListOutlined
    )

    data object Checker : Screen(
        AppRoute.Checker(),
        R.string.nav_checker,
        AppIcons.Analytics,
        AppIcons.AnalyticsOutlined
    )

    data object About : Screen(
        AppRoute.About,
        R.string.nav_about,
        AppIcons.Info,
        AppIcons.InfoOutlined
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Filters,
    Screen.GeneratedGames,
    Screen.Checker,
    Screen.About
)

/**
 * Navigates to the Checker screen with a preselected set of numbers.
 */
fun NavController.navigateToChecker(numbers: Set<Int>) {
    val list = numbers.sorted()
    // Pass numbers via SavedStateHandle to avoid creating multiple typed routes
    // and to allow bottom-navigation style single-instance behavior with state restoration.
    currentBackStackEntry?.savedStateHandle?.set("checker_numbers", list)
    navigate(AppRoute.Checker()) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
