package com.cebolao.lotofacil.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppIcons

/**
 * Metadata wrapper for Navigation Destinations, primarily for the Bottom Navigation Bar.
 */
@Stable
sealed class Screen(
    val routeObject: Any,
    @get:StringRes val titleRes: Int? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Home : Screen(
        HomeRoute,
        R.string.nav_home,
        AppIcons.Home,
        AppIcons.HomeOutlined
    )

    data object Filters : Screen(
        FiltersRoute,
        R.string.nav_filters,
        AppIcons.Tune,
        AppIcons.TuneOutlined
    )

    data object GeneratedGames : Screen(
        GeneratedGamesRoute,
        R.string.nav_games,
        AppIcons.List,
        AppIcons.ListOutlined
    )

    data object Checker : Screen(
        CheckerRoute(),
        R.string.nav_checker,
        AppIcons.Analytics,
        AppIcons.AnalyticsOutlined
    )

    data object About : Screen(
        AboutRoute,
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
 * Navigates to the Checker screen with a preselected set of numbers.  The numbers are
 * converted to a sorted list and wrapped in a `CheckerRoute` object.  This extension
 * centralises the navigation logic so callers don't need to be aware of route objects.
 */
fun NavController.navigateToChecker(numbers: Set<Int>) {
    val list = numbers.sorted()
    navigate(CheckerRoute(list)) {
        launchSingleTop = true
        restoreState = true
    }
}
