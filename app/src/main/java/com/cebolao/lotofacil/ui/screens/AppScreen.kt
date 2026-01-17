package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.components.layout.StandardScreenHeader

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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            StandardScreenHeader(
                title = title,
                subtitle = subtitle,
                navigationIcon = { navigationIcon?.invoke() },
                actions = actions,
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f)
            )
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost
    ) { innerPadding ->
        content(innerPadding)
    }
}
