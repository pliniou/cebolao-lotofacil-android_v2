package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Paletas de cores de destaque disponíveis para o tema.
 * Cada paleta define a cor primária que será usada em todo o tema.
 */
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
            AccentPalette.AZUL -> BrandRoxo
            else -> BrandRosa
        }
    
    private fun backgroundLayer(level: Int = 0): Color = when {
        isDark -> when (level) {
            0 -> DarkBackground
            1 -> DarkSurface1
            2 -> DarkSurface2
            else -> DarkSurface3
        }
        else -> when (level) {
            0 -> LightBackground
            1 -> LightSurface1
            2 -> LightSurface2
            else -> LightSurface3
        }
    }
    
    private fun contentColor(emphasis: Int = 0): Color = when {
        isDark -> when (emphasis) {
            0 -> DarkTextPrimary
            1 -> DarkTextSecondary
            else -> DarkTextTertiary
        }
        else -> when (emphasis) {
            0 -> LightTextPrimary
            1 -> LightTextSecondary
            else -> LightTextTertiary
        }
    }
    
    private fun outlineColor(variant: Boolean = false): Color = when {
        isDark -> if (variant) DarkOutlineVariant else DarkOutline
        else -> if (variant) LightOutlineVariant else LightOutline
    }
    
    fun build(): ColorScheme = if (isDark) buildDark() else buildLight()
    
    private fun buildDark() = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.18f), // Melhor visibilidade em dark mode
        onPrimaryContainer = primary,

        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.copy(alpha = 0.18f),
        onSecondaryContainer = secondary,

        tertiary = BrandAmarelo,
        onTertiary = Color.Black,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.18f),
        onTertiaryContainer = BrandAmarelo,

        background = backgroundLayer(0),
        onBackground = contentColor(0),

        surface = backgroundLayer(1),
        onSurface = contentColor(0),
        surfaceVariant = backgroundLayer(2),
        onSurfaceVariant = contentColor(1),

        surfaceContainerLowest = backgroundLayer(1),
        surfaceContainerLow = backgroundLayer(1),
        surfaceContainer = backgroundLayer(2),
        surfaceContainerHigh = backgroundLayer(3),
        surfaceContainerHighest = backgroundLayer(3),
        
        surfaceBright = Slate800,
        surfaceDim = DarkBackground,

        outline = outlineColor(false),
        outlineVariant = outlineColor(true),

        error = ErrorBase,
        onError = Color.White,
        errorContainer = ErrorBase.copy(alpha = 0.18f),
        onErrorContainer = ErrorBase,

        surfaceTint = primary,
        inverseSurface = LightTextPrimary,
        inverseOnSurface = LightBackground,
        inversePrimary = primary,
        scrim = Color.Black.copy(alpha = Alpha.SCRIM)
    )
    
    private fun buildLight() = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.10f), // Melhor visibilidade em light mode
        onPrimaryContainer = primary,

        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.copy(alpha = 0.10f),
        onSecondaryContainer = secondary,

        tertiary = BrandAmarelo,
        onTertiary = Color.Black,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.10f),
        onTertiaryContainer = Color.Black,

        background = backgroundLayer(0),
        onBackground = contentColor(0),

        surface = backgroundLayer(1),
        onSurface = contentColor(0),
        surfaceVariant = backgroundLayer(2),
        onSurfaceVariant = contentColor(1),

        surfaceContainerLowest = Color.White,
        surfaceContainerLow = LightSurface2,
        surfaceContainer = LightSurface1,
        surfaceContainerHigh = LightSurface2,
        surfaceContainerHighest = LightSurface3,
        
        surfaceBright = Color.White,
        surfaceDim = Slate100,

        outline = outlineColor(false),
        outlineVariant = outlineColor(true),

        error = ErrorBase,
        onError = Color.White,
        errorContainer = ErrorBase.copy(alpha = 0.14f), // Melhor contraste
        onErrorContainer = ErrorBase,

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
