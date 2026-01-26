package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.compose.material3.surfaceColorAtElevation
import com.cebolao.lotofacil.navigation.AppRoute
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.bottomNavItems
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyBody
import com.cebolao.lotofacil.ui.theme.Motion

/**
 * Barra de navegacao inferior com animacoes elegantes.
 */
@Composable
fun AppBottomBar(navController: NavHostController, currentDestination: NavDestination?) {
    val isOnboarding = remember(currentDestination) {
        currentDestination?.hierarchySequence()?.any {
            it.route?.startsWith(AppRoute.Onboarding::class.qualifiedName.orEmpty()) == true
        } == true
    }

    val isVisible = currentDestination != null && !isOnboarding

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
        NavigationBarContainer(isDark = isDark) {
            val isResults = currentDestination?.hierarchySequence()?.any {
                it.route?.startsWith(AppRoute.Results::class.qualifiedName.orEmpty()) == true
            } == true

            bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchySequence()?.any {
                    it.route?.startsWith(screen.route::class.qualifiedName ?: "") == true
                } == true || (screen is Screen.Home && isResults)

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
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamilyBody,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = scheme.secondaryContainer.copy(alpha = 0.9f),
                        selectedIconColor = scheme.onSecondaryContainer,
                        selectedTextColor = scheme.onSurface,
                        unselectedIconColor = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
private fun NavigationBarContainer(
    isDark: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val barShape = MaterialTheme.shapes.large

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.Spacing16, vertical = Dimen.Spacing8)
            .shadow(elevation = 8.dp, shape = barShape, ambientColor = scheme.primary.copy(alpha = 0.08f), spotColor = scheme.primary.copy(alpha = 0.08f))
            .clip(barShape),
        containerColor = scheme.surfaceColorAtElevation(if (isDark) 8.dp else 6.dp),
        tonalElevation = 0.dp,
        contentColor = scheme.onSurface
    ) {
        content()
    }
}

private fun NavDestination.hierarchySequence(): Sequence<NavDestination> =
    generateSequence(this) { it.parent }
