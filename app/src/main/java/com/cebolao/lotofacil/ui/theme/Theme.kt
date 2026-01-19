package com.cebolao.lotofacil.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.os.Build

@Composable
fun CebolaoLotofacilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentPalette: AccentPalette = AccentPalette.AZUL,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorSchemeFor(accentPalette)
        else -> lightColorSchemeFor(accentPalette)
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

