package com.cebolao.lotofacil.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cebolao.lotofacil.ui.screens.AboutScreen
import com.cebolao.lotofacil.ui.screens.CheckerScreen
import com.cebolao.lotofacil.ui.screens.FiltersScreen
import com.cebolao.lotofacil.ui.screens.GeneratedGamesScreen
import com.cebolao.lotofacil.ui.screens.HomeScreen
import com.cebolao.lotofacil.ui.screens.OnboardingScreen
import com.cebolao.lotofacil.viewmodels.MainViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { 
            androidx.compose.animation.slideInHorizontally(
                initialOffsetX = { (it * 0.1f).toInt() },
                animationSpec = com.cebolao.lotofacil.ui.theme.Motion.Tween.medium()
            ) + fadeIn(com.cebolao.lotofacil.ui.theme.Motion.Tween.medium()) 
        },
        exitTransition = { 
            androidx.compose.animation.slideOutHorizontally(
                targetOffsetX = { -(it * 0.1f).toInt() },
                animationSpec = com.cebolao.lotofacil.ui.theme.Motion.Tween.medium()
            ) + fadeOut(com.cebolao.lotofacil.ui.theme.Motion.Tween.fast()) 
        },
        popEnterTransition = {
            androidx.compose.animation.slideInHorizontally(
                initialOffsetX = { -(it * 0.1f).toInt() },
                animationSpec = com.cebolao.lotofacil.ui.theme.Motion.Tween.medium()
            ) + fadeIn(com.cebolao.lotofacil.ui.theme.Motion.Tween.medium())
        },
        popExitTransition = {
            androidx.compose.animation.slideOutHorizontally(
                targetOffsetX = { (it * 0.1f).toInt() },
                animationSpec = com.cebolao.lotofacil.ui.theme.Motion.Tween.medium()
            ) + fadeOut(com.cebolao.lotofacil.ui.theme.Motion.Tween.fast())
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen {
                mainViewModel.onOnboardingComplete()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Filters.route) { FiltersScreen(navController) }
        composable(Screen.GeneratedGames.route) { _ ->
            val canPop = navController.previousBackStackEntry != null
            GeneratedGamesScreen(
                navController = navController,
                onNavigateBack = if (canPop) {
                    { navController.popBackStack() }
                } else null
            ) 
        }
        composable(Screen.Checker.route, Screen.Checker.arguments) { backStackEntry ->
             val hasNumbersArg = backStackEntry.arguments?.getString(Screen.Checker.ARG_NUMBERS) != null
             
             CheckerScreen(
                 onNavigateBack = if (hasNumbersArg) {
                     { navController.popBackStack() }
                 } else null
             )
        }
        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}
