package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.cebolao.lotofacil.navigation.bottomNavItems
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyBody
import com.cebolao.lotofacil.ui.theme.GlassSurfaceDark
import com.cebolao.lotofacil.ui.theme.GlassSurfaceLight
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Barra de navegacao inferior com animacoes elegantes.
 */
@Composable
fun AppBottomBar(navController: NavHostController, currentDestination: NavDestination?) {
    // Check if the current destination is one of the bottom nav items
    val isVisible = remember(currentDestination) {
        bottomNavItems.any { screen ->
            currentDestination?.hierarchySequence()?.any {
                it.route?.startsWith(screen.route::class.qualifiedName ?: "") == true
            } == true
        }
    }

    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

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
            containerColor = if (isDark) GlassSurfaceDark else GlassSurfaceLight,
            tonalElevation = Dimen.Elevation.None,
            modifier = Modifier.drawBehind {
                drawLine(
                    color = scheme.outlineVariant.copy(alpha = 0.2f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = Dimen.Border.Thin.toPx()
                )
            }
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchySequence()?.any {
                    it.route?.startsWith(screen.route::class.qualifiedName ?: "") == true
                } == true

                val label = screen.titleRes?.let { stringResource(it) } ?: ""

                NavigationBarItem(
                    selected = selected,
                    alwaysShowLabel = true,
                    onClick = {
                        if (selected) return@NavigationBarItem

                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        val iconModifier = Modifier.size(Dimen.IconMedium)
                        (if (selected) screen.selectedIcon else screen.unselectedIcon)?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = label,
                                modifier = iconModifier
                            )
                        }
                    },
                    label = {
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamilyBody,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = scheme.primaryContainer.copy(alpha = 0.7f),
                        selectedIconColor = scheme.onPrimaryContainer,
                        selectedTextColor = scheme.primary,
                        unselectedIconColor = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = scheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

private fun NavDestination.hierarchySequence(): Sequence<NavDestination> =
    generateSequence(this) { it.parent }
