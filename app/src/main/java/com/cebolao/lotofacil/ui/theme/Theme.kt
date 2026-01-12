package com.cebolao.lotofacil.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            
            // Modern edge-to-edge experience
            val statusBarColor = colorScheme.background
            val navigationBarColor = colorScheme.surfaceContainer
            
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
            
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = statusBarColor.luminance() > 0.5f
                isAppearanceLightNavigationBars = navigationBarColor.luminance() > 0.5f
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes, // Shapes using our Dimen tokens
        content = content
    )
}

