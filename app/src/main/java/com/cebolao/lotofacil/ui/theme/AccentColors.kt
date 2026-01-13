package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class AccentPalette(val seed: Color) {
    AZUL(BrandAzul),
    ROXO(BrandRoxo),
    VERDE(BrandVerde),
    AMARELO(BrandAmarelo),
    ROSA(BrandRosa),
    LARANJA(BrandLaranja)
}

private class ColorSchemeBuilder(
    private val palette: AccentPalette,
    private val isDark: Boolean
) {
    private val primary = palette.seed
    
    private val onPrimary: Color
        get() = if (primary.luminance() > 0.5f) Color.Black else Color.White
    
    private val secondary: Color
        get() = when (palette) {
            AccentPalette.ROSA -> BrandAzul
            AccentPalette.AZUL -> BrandRosa
            else -> BrandRosa
        }
    
    private fun backgroundLayer(depth: Int = 0): Color = when {
        isDark -> when (depth) {
            0 -> DarkBackground
            1 -> DarkSurface
            2 -> DarkSurfaceElevated
            else -> DarkSurfaceHighlight
        }
        else -> when (depth) {
            0 -> LightBackground
            1 -> LightSurface
            2 -> LightSurfaceElevated
            else -> LightSurfaceHighlight
        }
    }
    
    private fun contentColor(emphasis: Int = 0): Color = when {
        isDark -> when (emphasis) {
            0 -> TextPrimaryDark
            1 -> TextSecondaryDark
            else -> TextTertiaryDark
        }
        else -> when (emphasis) {
            0 -> TextPrimaryLight
            1 -> TextSecondaryLight
            else -> TextTertiaryLight
        }
    }
    
    private fun outlineColor(variant: Boolean = false): Color = when {
        isDark -> if (variant) Slate700.copy(alpha = 0.4f) else Slate700
        else -> if (variant) Slate300 else Slate400
    }
    
    fun build(): ColorScheme = if (isDark) buildDark() else buildLight()
    
    private fun buildDark() = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = Alpha.DIVIDER),
        onPrimaryContainer = primary,

        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.copy(alpha = Alpha.DIVIDER),
        onSecondaryContainer = secondary,

        tertiary = BrandAmarelo,
        onTertiary = Color.Black,
        tertiaryContainer = BrandAmarelo.copy(alpha = Alpha.DIVIDER),
        onTertiaryContainer = BrandAmarelo,

        background = backgroundLayer(0),
        onBackground = contentColor(0),

        surface = backgroundLayer(1),
        onSurface = contentColor(0),
        surfaceVariant = backgroundLayer(2),
        onSurfaceVariant = contentColor(1),

        surfaceContainerLowest = backgroundLayer(0),
        surfaceContainerLow = backgroundLayer(0),
        surfaceContainer = backgroundLayer(1),
        surfaceContainerHigh = backgroundLayer(2),
        surfaceContainerHighest = backgroundLayer(3),
        
        surfaceBright = Slate800,
        surfaceDim = Slate950,

        outline = outlineColor(false),
        outlineVariant = outlineColor(true),

        error = ErrorColor,
        onError = Color.White,
        errorContainer = ErrorColor.copy(alpha = Alpha.DIVIDER),
        onErrorContainer = ErrorColor,

        surfaceTint = primary,
        inverseSurface = contentColor(0),
        inverseOnSurface = backgroundLayer(0),
        inversePrimary = primary,
        scrim = Color.Black.copy(alpha = Alpha.SCRIM)
    )
    
    private fun buildLight() = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.2f),
        onPrimaryContainer = primary,

        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.copy(alpha = 0.2f),
        onSecondaryContainer = secondary,

        tertiary = BrandAmarelo,
        onTertiary = Color.Black,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.2f),
        onTertiaryContainer = Color.Black,

        background = backgroundLayer(0),
        onBackground = contentColor(0),

        surface = backgroundLayer(1),
        onSurface = contentColor(0),
        surfaceVariant = backgroundLayer(2),
        onSurfaceVariant = contentColor(1),

        surfaceContainerLowest = backgroundLayer(1),
        surfaceContainerLow = backgroundLayer(2),
        surfaceContainer = backgroundLayer(1),
        surfaceContainerHigh = backgroundLayer(2),
        surfaceContainerHighest = backgroundLayer(3),
        
        surfaceBright = Color.White,
        surfaceDim = Slate100,

        outline = outlineColor(false),
        outlineVariant = outlineColor(true),

        error = ErrorColor,
        onError = Color.White,
        errorContainer = ErrorColor.copy(alpha = 0.2f),
        onErrorContainer = ErrorColor,

        surfaceTint = primary,
        inverseSurface = Slate900,
        inverseOnSurface = Slate100,
        inversePrimary = primary,
        scrim = Color.Black.copy(alpha = Alpha.SCRIM)
    )
}

/**
 * Generates dark color scheme for the given accent palette.
 * Uses recursive builder pattern for consistency.
 */
fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    return ColorSchemeBuilder(palette, isDark = true).build()
}

fun lightColorSchemeFor(palette: AccentPalette): ColorScheme {
    return ColorSchemeBuilder(palette, isDark = false).build()
}
