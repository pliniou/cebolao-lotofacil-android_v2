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
    accentPalette: AccentPalette = AccentPalette.AZUL, // Default to AZUL
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
 * Replaces the legacy Enum with a Sealed Class for extensibility while maintaining compatibility.
 */
sealed class AccentPalette(
    val name: String,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color
) {
    object AZUL : AccentPalette(
        name = "AZUL",
        primary = BrandAzul,
        primaryContainer = BrandAzul.copy(alpha = 0.1f),
        secondary = BrandRoxo,
        secondaryContainer = BrandRoxo.copy(alpha = 0.1f),
        tertiary = BrandAmarelo,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.1f)
    )

    object ROXO : AccentPalette(
        name = "ROXO",
        primary = BrandRoxo,
        primaryContainer = BrandRoxo.copy(alpha = 0.1f),
        secondary = BrandRosa, // Default secondary
        secondaryContainer = BrandRosa.copy(alpha = 0.1f),
        tertiary = BrandAmarelo,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.1f)
    )

    object VERDE : AccentPalette(
        name = "VERDE",
        primary = BrandVerde,
        primaryContainer = BrandVerde.copy(alpha = 0.1f),
        secondary = BrandRosa,
        secondaryContainer = BrandRosa.copy(alpha = 0.1f),
        tertiary = BrandAmarelo,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.1f)
    )

    object AMARELO : AccentPalette(
        name = "AMARELO",
        primary = BrandAmarelo,
        primaryContainer = BrandAmarelo.copy(alpha = 0.1f),
        secondary = BrandRosa,
        secondaryContainer = BrandRosa.copy(alpha = 0.1f),
        tertiary = BrandAmarelo, // Tertiary same as primary? Or maybe fallback.
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.1f)
    )

    object ROSA : AccentPalette(
        name = "ROSA",
        primary = BrandRosa,
        primaryContainer = BrandRosa.copy(alpha = 0.1f),
        secondary = BrandAzul, // Special case
        secondaryContainer = BrandAzul.copy(alpha = 0.1f),
        tertiary = BrandAmarelo,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.1f)
    )

    object LARANJA : AccentPalette(
        name = "LARANJA",
        primary = BrandLaranja,
        primaryContainer = BrandLaranja.copy(alpha = 0.1f),
        secondary = BrandRosa,
        secondaryContainer = BrandRosa.copy(alpha = 0.1f),
        tertiary = BrandAmarelo,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.1f)
    )

    companion object {
        val entries = listOf(AZUL, ROXO, VERDE, AMARELO, ROSA, LARANJA)
        val Default = AZUL
        // Helper for legacy enum-like lookups
        fun valueOf(name: String): AccentPalette = entries.find { it.name == name } ?: Default
    }
}


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
    background = Color(0xFFFAFAFA),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF747474),
    outlineVariant = Color(0xFFBDBDBD),
    surfaceVariant = Color(0xFFE7E7E7),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceTint = Color(0xFFE7E7E7),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F2F2F),
    inverseOnSurface = Color.White,
    inversePrimary = palette.primary,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFDADADA),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerLow = Color(0xFFF5F5F5),
    surfaceContainerLowest = Color.White
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
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD4),
    outline = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF49454F),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceTint = Color(0xFF1E1E1E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFEDEDED),
    inverseOnSurface = Color.Black,
    inversePrimary = palette.primary,
    surfaceBright = Color(0xFF343434),
    surfaceDim = Color(0xFF1E1E1E),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF343434),
    surfaceContainerHighest = Color(0xFF49454F),
    surfaceContainerLow = Color(0xFF1E1E1E),
    surfaceContainerLowest = Color(0xFF000000)
)
