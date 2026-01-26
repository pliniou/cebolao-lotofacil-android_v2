package com.cebolao.lotofacil.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * App theme that supports dynamic colours on Android 12+ and falls back to predefined colour schemes.
 */
@Composable
fun CebolaoLotofacilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentPalette: AccentPalette = DefaultAccentPalette,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(accentPalette)
        else -> lightColorScheme(accentPalette)
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
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF410E0B),
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceVariant = LightSurface3,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = palette.primary,
    scrim = Color(0xFF000000),
    inverseSurface = LightSurface3,
    inverseOnSurface = LightTextPrimary,
    inversePrimary = palette.primary,
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
    onPrimary = Color.Black,
    primaryContainer = palette.primaryContainer,
    onPrimaryContainer = Color.White,
    secondary = palette.secondary,
    onSecondary = Color.Black,
    secondaryContainer = palette.secondaryContainer,
    onSecondaryContainer = Color.White,
    tertiary = palette.tertiary,
    onTertiary = Color.Black,
    tertiaryContainer = palette.tertiaryContainer,
    onTertiaryContainer = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface1,
    onSurface = DarkTextPrimary,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD4),
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceVariant = DarkSurface3,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = palette.primary,
    scrim = Color(0xFF000000),
    inverseSurface = LightSurface1,
    inverseOnSurface = DarkTextPrimary,
    inversePrimary = palette.primary,
    surfaceBright = DarkSurface3,
    surfaceDim = DarkSurface1,
    surfaceContainer = DarkSurface2,
    surfaceContainerHigh = DarkSurface2,
    surfaceContainerHighest = DarkSurface3,
    surfaceContainerLow = DarkSurface1,
    surfaceContainerLowest = DarkSurface1
)
