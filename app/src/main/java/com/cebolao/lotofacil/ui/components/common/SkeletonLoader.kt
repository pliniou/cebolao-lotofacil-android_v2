package com.cebolao.lotofacil.ui.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Skeleton loading box with shimmer animation effect.
 * Provides visual feedback during content loading states.
 *
 * @param modifier Modifier for the skeleton box
 * @param shape Shape of the skeleton (defaults to rounded corners)
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimen.CornerRadiusSmall)
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 300f, translateAnim + 300f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Skeleton loader for game cards.
 * Displays a placeholder with shimmer effect while game data loads.
 */
@Composable
fun GameCardSkeleton(modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Outlined
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                )
                SkeletonBox(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape
                )
            }
            
            // Number balls grid (15 balls in 3 rows of 5)
            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
                ) {
                    repeat(5) {
                        SkeletonBox(
                            modifier = Modifier.size(Dimen.BallSizeMedium),
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    }
}

/**
 * Skeleton loader for statistics cards.
 * Displays a placeholder with shimmer effect while statistics data loads.
 */
@Composable
fun StatisticsCardSkeleton(modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Outlined
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing12)
        ) {
            // Title
            SkeletonBox(
                modifier = Modifier
                    .width(150.dp)
                    .height(24.dp)
            )
            
            // Chart area
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.BarChartHeight)
            )
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(40.dp)
                                .height(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Skeleton loader for draw history list items.
 */
@Composable
fun DrawHistorySkeleton(modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Outlined
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.Spacing16),
            horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {
            // Draw number
            SkeletonBox(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    repeat(5) {
                        SkeletonBox(
                            modifier = Modifier.size(Dimen.BallSizeSmall),
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun SkeletonLoadersPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
        ) {
            GameCardSkeleton()
            StatisticsCardSkeleton()
            DrawHistorySkeleton()
        }
    }
}
