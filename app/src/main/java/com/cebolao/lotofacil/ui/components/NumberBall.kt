package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyNumeric
import com.cebolao.lotofacil.ui.theme.Motion
import com.cebolao.lotofacil.ui.theme.SuccessColor
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

enum class NumberBallSize { Large, Medium, Small }
enum class NumberBallVariant { Primary, Secondary, Neutral, Hit, Miss }

@Immutable
private data class BallColors(
    val container: Color,
    val content: Color,
    val border: Color
)

private object BallConfig {
    @Composable
    fun modernShape() = MaterialTheme.shapes.small
    
    val sizeMap = mapOf(
        NumberBallSize.Large to Dimen.BallSizeLarge,
        NumberBallSize.Medium to Dimen.BallSizeMedium,
        NumberBallSize.Small to Dimen.BallSizeSmall
    )
    
    val fontSizeMap = mapOf(
        NumberBallSize.Large to Dimen.BallTextLarge,
        NumberBallSize.Medium to Dimen.BallTextMedium,
        NumberBallSize.Small to Dimen.BallTextSmall
    )
}

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    sizeVariant: NumberBallSize = NumberBallSize.Medium,
    isSelected: Boolean = false,
    isDisabled: Boolean = false,
    variant: NumberBallVariant = NumberBallVariant.Neutral,
    customBackgroundColor: Color? = null
) {
    val size = BallConfig.sizeMap.getValue(sizeVariant)
    val fontSize = BallConfig.fontSizeMap.getValue(sizeVariant)
    val scheme = MaterialTheme.colorScheme
    val colors = remember(isSelected, variant, scheme, customBackgroundColor) {
        resolveColors(isSelected, variant, scheme, customBackgroundColor)
    }

    val background by animateColorAsState(colors.container, label = "ballBackground")
    val content by animateColorAsState(colors.content, label = "ballContent")
    val targetScale by remember {
        derivedStateOf {
            when {
                isSelected -> Motion.Offset.SELECTSCALE
                variant == NumberBallVariant.Hit -> 1.08f
                else -> 1f
            }
        }
    }
    
    val ballScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = Motion.Spring.snappy(),
        label = "ballScale"
    )

    val statusString = when {
        isSelected -> stringResource(com.cebolao.lotofacil.R.string.general_selected)
        variant == NumberBallVariant.Hit -> "Acertou"
        variant == NumberBallVariant.Miss -> "Errou"
        else -> "Neutro"
    }
    val contentDesc = stringResource(
        com.cebolao.lotofacil.R.string.number_ball_content_description,
        number,
        statusString
    )

    Surface(
        modifier = modifier
            .size(size)
            .scale(ballScale)
            .alpha(if (isDisabled) Alpha.DISABLED else 1f)
            .semantics { this.contentDescription = contentDesc },
        shape = BallConfig.modernShape(),
        color = background,
        contentColor = content,
        shadowElevation = 0.dp,
        border = if (colors.border != Color.Transparent) {
            BorderStroke(Dimen.Border.Hairline, colors.border)
        } else {
            null
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = DEFAULT_NUMBER_FORMAT.format(number),
                fontFamily = FontFamilyNumeric,
                fontSize = fontSize,
                fontWeight = if (isSelected || variant == NumberBallVariant.Hit) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                }
            )
        }
    }
}

private fun resolveColors(
    isSelected: Boolean,
    variant: NumberBallVariant,
    scheme: ColorScheme,
    customBackgroundColor: Color?
): BallColors {
    // Selection always overrides everything
    if (isSelected) {
        // If there's a custom background (heatmap), we might want to blend or show distinct border?
        // Standard user expectation: Selection = Primary color.
        return BallColors(
            container = scheme.primary,
            content = scheme.onPrimary,
            border = Color.Transparent
        )
    }

    // Custom background (Heatmap etc) overrides standard variants (except Hit/Miss which are strong states)
    if (customBackgroundColor != null && variant == NumberBallVariant.Neutral) {
        return BallColors(
            container = customBackgroundColor,
            // Simple heuristic for content color: assume heatmap colors are somewhat dark/saturated
            // or we use onSurface if it's light?
            // For safety and contrast with typical Red/Blue heatmaps: White is usually good.
            // But if it creates low contrast on light yellow...
            // Let's use scheme.invOnSurface? No.
            // Let's stick to scheme.onPrimaryContainer if null, but here we have custom color.
            // Let's force White for now as it looks professional on colored dots.
            content = Color.White, 
            border = Color.Transparent
        )
    }

    return when {
        variant == NumberBallVariant.Hit -> BallColors(
            container = SuccessColor,
            content = Color.White,
            border = Color.Transparent
        )

        variant == NumberBallVariant.Miss -> BallColors(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            border = Color.Transparent
        )
        
        variant == NumberBallVariant.Primary -> BallColors(
            container = scheme.primaryContainer,
            content = scheme.onPrimaryContainer,
            border = Color.Transparent
        )
        
        variant == NumberBallVariant.Secondary -> BallColors(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            border = Color.Transparent
        )

        variant == NumberBallVariant.Neutral -> BallColors(
            container = scheme.surfaceContainerHigh,
            content = scheme.onSurface,
            border = Color.Transparent
        )

        else -> BallColors(
            container = scheme.surface,
            content = scheme.onSurface,
            border = scheme.outlineVariant
        )
    }
}
