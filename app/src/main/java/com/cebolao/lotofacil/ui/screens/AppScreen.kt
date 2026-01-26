package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import com.cebolao.lotofacil.ui.components.layout.ModernBackground
import com.cebolao.lotofacil.ui.components.layout.StandardScreenHeader
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        ModernBackground()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                StandardScreenHeader(
                    title = title,
                    subtitle = subtitle,
                    navigationIcon = { navigationIcon?.invoke() },
                    actions = actions,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) // Glassy header
                )
            },
            bottomBar = bottomBar,
            snackbarHost = snackbarHost
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val mergedPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection) + Dimen.ScreenPadding,
                end = innerPadding.calculateEndPadding(layoutDirection) + Dimen.ScreenPadding,
                top = innerPadding.calculateTopPadding() + Dimen.Spacing12,
                bottom = innerPadding.calculateBottomPadding() + Dimen.Spacing12
            )
            content(mergedPadding)
        }
    }
}
