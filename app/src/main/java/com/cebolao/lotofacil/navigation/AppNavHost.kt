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
import com.cebolao.lotofacil.presentation.viewmodel.MainUiEvent
import com.cebolao.lotofacil.presentation.viewmodel.MainViewModel
import com.cebolao.lotofacil.ui.screens.AboutScreen
import com.cebolao.lotofacil.ui.screens.CheckerScreen
import com.cebolao.lotofacil.ui.screens.FiltersScreen
import com.cebolao.lotofacil.ui.screens.GeneratedGamesScreen
import com.cebolao.lotofacil.ui.screens.HomeScreen
import com.cebolao.lotofacil.ui.screens.OnboardingScreen
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { (it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium()
            ) + fadeIn(Motion.Tween.medium())
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -(it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium()
            ) + fadeOut(Motion.Tween.fast())
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -(it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium()
            ) + fadeIn(Motion.Tween.medium())
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (it * 0.1f).toInt() },
                animationSpec = Motion.Tween.medium()
            ) + fadeOut(Motion.Tween.fast())
        }
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen {
                mainViewModel.onEvent(MainUiEvent.CompleteOnboarding)
                navController.navigate(HomeRoute) {
                    popUpTo<OnboardingRoute> { inclusive = true }
                }
            }
        }
        composable<HomeRoute> { HomeScreen(navController) }
        composable<FiltersRoute> { FiltersScreen(navController) }
        composable<GeneratedGamesRoute> {
            val canPop = navController.previousBackStackEntry != null
            GeneratedGamesScreen(
                navController = navController,
                onNavigateBack = if (canPop) {
                    { navController.popBackStack() }
                } else null
            )
        }
        composable<CheckerRoute> {
            // The typed `CheckerRoute` supplies a `numbers` list.  The `CheckerViewModel` retrieves
            // these arguments from its `SavedStateHandle` via `toRoute<CheckerRoute>()`.  We avoid
            // passing the list into the composable directly to keep the navigation layer free of
            // presentation-layer logic.
            CheckerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<AboutRoute> { AboutScreen() }
    }
}
