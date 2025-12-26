package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.cebolao.lotofacil.navigation.bottomNavItems
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.ui.theme.Outfit

/**
 * Barra de navegação inferior com animações elegantes.
 * 
 * - Animação de slide + fade ao entrar/sair
 * - Elevação sutil para profundidade visual
 * - Indicadores de seleção com cores temáticas
 */
@Composable
fun AppBottomBar(navController: NavHostController, currentDestination: NavDestination?) {
    val isVisible by remember(currentDestination) {
        derivedStateOf {
            bottomNavItems.any {
                it.baseRoute == currentDestination?.route?.substringBefore('?')
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = Motion.Tween.enter()
        ) + fadeIn(animationSpec = Motion.Tween.medium()),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = Motion.Tween.exit()
        ) + fadeOut(animationSpec = Motion.Tween.fast())
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer, // Flat/Tonal background
            tonalElevation = Dimen.Elevation.None
        ) {
            bottomNavItems.forEach { screen ->
                val selected =
                    currentDestination?.hierarchy?.any {
                        it.route?.substringBefore('?') == screen.baseRoute
                    } == true
                    
                val label = screen.titleRes?.let { stringResource(it) } ?: ""

                NavigationBarItem(
                    selected = selected,
                    alwaysShowLabel = true,
                    onClick = {
                        // Evita navegar para a mesma tela
                        if (selected) return@NavigationBarItem
                        
                        navController.navigate(screen.route) {
                            // Apenas limpa backstack ao navegar para destinos principais
                            // Permite que back gesture funcione naturalmente
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        (if (selected) screen.selectedIcon else screen.unselectedIcon)?.let {
                            Icon(it, contentDescription = label)
                        }
                    },
                    label = {
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = Outfit,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
