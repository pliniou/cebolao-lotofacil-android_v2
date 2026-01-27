package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

enum class GlassIntensity {
    High, Medium, Low
}

/**
 * Applies a premium Glassmorphism effect to the component.
 * Uses a semi-transparent background with a subtle border to simulate depth.
 */
@Composable
fun Modifier.glass(
    shape: Shape = MaterialTheme.shapes.medium,
    strokeWidth: Dp = Dimen.Border.Thin,
    intensity: GlassIntensity = GlassIntensity.Medium,
    darkTheme: Boolean = isSystemInDarkTheme()
): Modifier {
    val alpha = when (intensity) {
        GlassIntensity.High -> if (darkTheme) 0.85f else 0.90f
        GlassIntensity.Medium -> if (darkTheme) 0.65f else 0.72f
        GlassIntensity.Low -> if (darkTheme) 0.40f else 0.50f
    }

    val baseColor = if (darkTheme) GlassSurfaceDark else GlassSurfaceLight
    // Use the base color but override alpha based on intensity
    val backgroundColor = baseColor.copy(alpha = alpha)

    val borderBrush = if (darkTheme) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.20f)
            )
        )
    }

    return this
        .clip(shape)
        .background(color = backgroundColor, shape = shape)
        .border(width = strokeWidth, brush = borderBrush, shape = shape)
}

/**
 * A Card-like surface that applies the Glassmorphism effect.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    intensity: GlassIntensity = GlassIntensity.Medium,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glass(shape = shape, intensity = intensity)
    ) {
        content()
    }
}
