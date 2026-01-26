package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.DarkBackground
import com.cebolao.lotofacil.ui.theme.LightBackground
import androidx.compose.material3.surfaceColorAtElevation

@Composable
fun ModernBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val scheme = MaterialTheme.colorScheme
    val baseColor = if (darkTheme) DarkBackground else LightBackground
    val baseBrush = Brush.verticalGradient(
        colors = listOf(
            baseColor,
            scheme.surfaceColorAtElevation(4.dp),
            baseColor
        )
    )

    val animatedPrimary by animateColorAsState(
        targetValue = scheme.primary.copy(alpha = if (darkTheme) 0.22f else 0.14f),
        label = "primaryAmbient"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = scheme.secondary.copy(alpha = if (darkTheme) 0.16f else 0.1f),
        label = "secondaryAmbient"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "background_wave")
    val drift by infiniteTransition.animateFloat(
        initialValue = -60f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val driftSecondary by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftSecondary"
    )

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val offsetPx = with(density) { drift.dp.toPx() }
            val offsetSecondaryPx = with(density) { driftSecondary.dp.toPx() }
            val radius = size.minDimension * 0.75f
            val secondaryRadius = size.minDimension * 0.55f

            drawIntoCanvas {
                withTransform({
                    translate(offsetPx, offsetSecondaryPx / 2)
                }) {
                    drawCircle(
                        color = animatedPrimary,
                        radius = radius,
                        center = Offset(size.width * 0.25f, size.height * 0.25f)
                    )
                }
                withTransform({
                    translate(-offsetSecondaryPx, -offsetPx / 3)
                }) {
                    drawCircle(
                        color = animatedSecondary,
                        radius = secondaryRadius,
                        center = Offset(size.width * 0.8f, size.height * 0.75f)
                    )
                }
            }
        }
    }
}
