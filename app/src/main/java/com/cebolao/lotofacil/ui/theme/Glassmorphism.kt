package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a premium Glassmorphism effect to the component.
 * Uses a semi-transparent background with a subtle border.
 * Note: Realtime Blur (RenderEffect) is supported only on Android S+ (API 31+).
 * For now, we rely on semantic colors to simulate the effect.
 */
@Composable
fun Modifier.glass(
    shape: Shape = MaterialTheme.shapes.medium,
    strokeWidth: Dp = Dimen.Border.Thin,
    darkTheme: Boolean = isSystemInDarkTheme()
): Modifier {
    val backgroundColor = if (darkTheme) GlassSurfaceDark else GlassSurfaceLight
    val borderColor = if (darkTheme) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f)

    return this
        .clip(shape)
        .background(color = backgroundColor, shape = shape)
        .border(width = strokeWidth, color = borderColor, shape = shape)
}

/**
 * A Card-like surface that applies the Glassmorphism effect.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glass(shape = shape)
    ) {
        content()
    }
}
