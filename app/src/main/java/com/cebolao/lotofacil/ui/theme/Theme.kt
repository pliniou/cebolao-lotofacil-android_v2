@file:Suppress("DEPRECATION")

package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * App theme that supports dynamic colours on Android 12+ and falls back to predefined colour schemes.
 */
@Composable
fun CebolaoLotofacilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentPalette: AccentPalette = DefaultAccentPalette,
    content: @Composable () -> Unit
) {
    LocalContext.current
    
    // We want to force our premium theme over dynamic colors often, but let's keep it optional.
    // However, for "Premium" feel, consistent branding is usually better than dynamic wallpapers.
    // Let's stick to our palette to ensure the Glassmorphism works perfectly.
    val colorScheme: ColorScheme = when {
        darkTheme -> darkColorScheme(accentPalette)
        else -> lightColorScheme(accentPalette)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = rememberAdaptiveTypography(),
        shapes = Shapes,
        content = content
    )
}

/**
 * Represents the available accent colour palettes.
 */
data class AccentPalette(
    val name: String,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color
)

val AccentAzul = AccentPalette(
    name = "AZUL",
    primary = BrandAzul,
    primaryContainer = BrandAzulLight,
    secondary = BrandRoxo,
    secondaryContainer = BrandRoxoLight,
    tertiary = BrandAmarelo,
    tertiaryContainer = BrandAmareloLight
)

val AccentRoxo = AccentPalette(
    name = "ROXO",
    primary = BrandRoxo,
    primaryContainer = BrandRoxoLight,
    secondary = BrandRosa,
    secondaryContainer = BrandRosaLight,
    tertiary = BrandAmarelo,
    tertiaryContainer = BrandAmareloLight
)

val AccentVerde = AccentPalette(
    name = "VERDE",
    primary = BrandVerde,
    primaryContainer = BrandVerdeLight,
    secondary = BrandRosa,
    secondaryContainer = BrandRosaLight,
    tertiary = BrandAmarelo,
    tertiaryContainer = BrandAmareloLight
)

val AccentAmarelo = AccentPalette(
    name = "AMARELO",
    primary = BrandAmarelo,
    primaryContainer = BrandAmareloLight,
    secondary = BrandRosa,
    secondaryContainer = BrandRosaLight,
    tertiary = BrandAmarelo,
    tertiaryContainer = BrandAmareloLight
)

val AccentRosa = AccentPalette(
    name = "ROSA",
    primary = BrandRosa,
    primaryContainer = BrandRosaLight,
    secondary = BrandAzul,
    secondaryContainer = BrandAzulLight,
    tertiary = BrandAmarelo,
    tertiaryContainer = BrandAmareloLight
)

val AccentLaranja = AccentPalette(
    name = "LARANJA",
    primary = BrandLaranja,
    primaryContainer = BrandLaranjaLight,
    secondary = BrandRosa,
    secondaryContainer = BrandRosaLight,
    tertiary = BrandAmarelo,
    tertiaryContainer = BrandAmareloLight
)

val AccentPalettes = listOf(
    AccentAzul,
    AccentRoxo,
    AccentVerde,
    AccentAmarelo,
    AccentRosa,
    AccentLaranja
)

val DefaultAccentPalette = AccentAzul

fun accentPaletteByName(name: String): AccentPalette =
    AccentPalettes.find { it.name == name } ?: DefaultAccentPalette


private fun lightColorScheme(palette: AccentPalette): ColorScheme = androidx.compose.material3.lightColorScheme(
    primary = palette.primary,
    onPrimary = Color.White,
    primaryContainer = palette.primaryContainer,
    onPrimaryContainer = Color.Black,
    secondary = palette.secondary,
    onSecondary = Color.White,
    secondaryContainer = palette.secondaryContainer,
    onSecondaryContainer = Color.Black,
    tertiary = palette.tertiary,
    onTertiary = Color.White,
    tertiaryContainer = palette.tertiaryContainer,
    onTertiaryContainer = Color.Black,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface1,
    onSurface = LightTextPrimary,
    error = ErrorBase,
    onError = Color.White,
    errorContainer = ErrorLight,
    onErrorContainer = ErrorDark,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceVariant = LightSurface3,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = palette.primary,
    scrim = Color.Black.copy(alpha = Alpha.SCRIM),
    inverseSurface = DarkSurface1, // Inverse in light is dark
    inverseOnSurface = DarkTextPrimary,
    inversePrimary = palette.primary, // Often same or lighter in reverse
    surfaceBright = LightSurface1,
    surfaceDim = LightSurface3,
    surfaceContainer = LightSurface2,
    surfaceContainerHigh = LightSurface1,
    surfaceContainerHighest = LightSurface3,
    surfaceContainerLow = LightSurface2,
    surfaceContainerLowest = LightSurface1
)

private fun darkColorScheme(palette: AccentPalette): ColorScheme = androidx.compose.material3.darkColorScheme(
    primary = palette.primary,
    onPrimary = Color.White, // Premium dark often uses white on primary for punch
    primaryContainer = palette.primary.copy(alpha = 0.3f), // Glassy container
    onPrimaryContainer = Color.White,
    secondary = palette.secondary,
    onSecondary = Color.White,
    secondaryContainer = palette.secondary.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,
    tertiary = palette.tertiary,
    onTertiary = Color.Black,
    tertiaryContainer = palette.tertiary.copy(alpha = 0.3f),
    onTertiaryContainer = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface1,
    onSurface = DarkTextPrimary,
    error = ErrorBase,
    onError = Color.Black,
    errorContainer = ErrorDark,
    onErrorContainer = ErrorLight,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceVariant = DarkSurface3,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = palette.primary,
    scrim = Color.Black.copy(alpha = Alpha.SCRIM),
    inverseSurface = LightSurface1,
    inverseOnSurface = LightTextPrimary,
    inversePrimary = palette.primary,
    surfaceBright = DarkSurface2,
    surfaceDim = DarkSurface1,
    surfaceContainer = DarkSurface2,
    surfaceContainerHigh = DarkSurface2, // Elevating slightly
    surfaceContainerHighest = DarkSurface3,
    surfaceContainerLow = DarkSurface1,
    surfaceContainerLowest = Obsidian // Deepest black
)
