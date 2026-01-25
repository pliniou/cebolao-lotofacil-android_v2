package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.DarkBackground
import com.cebolao.lotofacil.ui.theme.LightBackground

@Composable
fun ModernBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val baseColor = if (darkTheme) DarkBackground else LightBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val animatedPrimary by animateColorAsState(primaryColor, label = "primary")
    val animatedSecondary by animateColorAsState(secondaryColor, label = "secondary")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // Ambient Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            baseColor,
                            animatedPrimary.copy(alpha = if (darkTheme) 0.1f else 0.05f),
                            baseColor
                        )
                    )
                )
        )

        // Animated Blobs (simulated mesh gradient)
        val infiniteTransition = rememberInfiniteTransition(label = "blobs")
        
        // Blob 1 Movement
        val blob1X by infiniteTransition.animateFloat(
            initialValue = -100f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blob1X"
        )
        val blob1Y by infiniteTransition.animateFloat(
            initialValue = -100f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blob1Y"
        )

        // Blob 2 Movement
        val blob2X by infiniteTransition.animateFloat(
            initialValue = 100f,
            targetValue = -100f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blob2X"
        )
        val blob2Y by infiniteTransition.animateFloat(
            initialValue = 100f,
            targetValue = -50f,
            animationSpec = infiniteRepeatable(
                animation = tween(18000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blob2Y"
        )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val blobSize = minOf(screenWidth, screenHeight) * 0.8f

        // Top-Left Blob (Primary)
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(blob1X.dp.roundToPx(), blob1Y.dp.roundToPx()) }
                .size(blobSize)
                .background(
                    color = animatedPrimary.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .blur(100.dp)
        )

        // Bottom-Right Blob (Secondary)
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset((screenWidth - blobSize + blob2X.dp).roundToPx(), (screenHeight - blobSize + blob2Y.dp).roundToPx()) }
                .size(blobSize)
                .background(
                    color = animatedSecondary.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .blur(100.dp)
        )
    }
    }
}


fun <T : Comparable<T>> minOf(a: T, b: T): T {
    return if (a <= b) a else b
}
