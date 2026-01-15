package com.cebolao.lotofacil.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
fun CebolaoLotofacilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentPalette: AccentPalette = AccentPalette.AZUL,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorSchemeFor(accentPalette)
    } else {
        lightColorSchemeFor(accentPalette)
    }

    // Side effect for system bars
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? ComponentActivity ?: return@SideEffect

            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = colorScheme.background.toArgb(),
                    darkScrim = colorScheme.background.toArgb()
                ) { darkTheme },
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = colorScheme.surfaceContainer.toArgb(),
                    darkScrim = colorScheme.surfaceContainer.toArgb()
                ) { darkTheme }
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes, // Shapes using our Dimen tokens
        content = content
    )
}

