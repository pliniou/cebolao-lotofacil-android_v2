package com.cebolao.lotofacil.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cebolao.lotofacil.presentation.viewmodel.MainUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.MainViewModel
import com.cebolao.lotofacil.ui.screens.AboutScreen
import com.cebolao.lotofacil.ui.screens.CheckerScreen
import com.cebolao.lotofacil.ui.screens.FiltersScreen
import com.cebolao.lotofacil.ui.screens.GeneratedGamesScreen
import com.cebolao.lotofacil.ui.screens.HomeScreen
import com.cebolao.lotofacil.ui.screens.OnboardingScreen
import com.cebolao.lotofacil.ui.screens.ResultsScreen
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.ui.util.MotionPreferences

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppRoute,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {
    val reduceMotion = MotionPreferences.rememberPrefersReducedMotion()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { (it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium(reduceMotion)
            ) + fadeIn(Motion.Tween.medium(reduceMotion))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -(it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium(reduceMotion)
            ) + fadeOut(Motion.Tween.fast(reduceMotion))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -(it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium(reduceMotion)
            ) + fadeIn(Motion.Tween.medium(reduceMotion))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium(reduceMotion)
            ) + fadeOut(Motion.Tween.fast(reduceMotion))
        }
    ) {
        composable<AppRoute.Onboarding> {
            OnboardingScreen {
                mainViewModel.onEvent(MainUiEvent.CompleteOnboarding)
                navController.navigate(AppRoute.Home) {
                    popUpTo(AppRoute.Onboarding) { inclusive = true }
                }
            }
        }
        composable<AppRoute.Home> {
            val hostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }
            val lazyState = androidx.compose.foundation.lazy.rememberLazyListState()

            HomeScreen(
                navController = navController,
                listState = lazyState,
                snackbarHostState = hostState
            )
        }
        composable<AppRoute.Results> {
            ResultsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<AppRoute.Filters> { FiltersScreen(navController) }
        composable<AppRoute.GeneratedGames> {
            val canPop = navController.previousBackStackEntry != null
            GeneratedGamesScreen(
                navController = navController,
                onNavigateBack = if (canPop) {
                    { navController.popBackStack() }
                } else null
            )
        }
        composable<AppRoute.Checker> { backStackEntry ->
// route not needed here, handled by ViewModel via SavedStateHandle
            CheckerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<AppRoute.About> { AboutScreen() }
    }
}
