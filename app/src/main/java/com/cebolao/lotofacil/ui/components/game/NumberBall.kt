package com.cebolao.lotofacil.ui.components.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Variant of number ball for different states
 */
enum class NumberBallVariant {
    Default,
    Hit,
    Miss
}

/**
 * Individual number ball component
 */
@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    variant: NumberBallVariant = NumberBallVariant.Default,
    @Suppress("UNUSED_PARAMETER") onClick: (() -> Unit)? = null
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        variant == NumberBallVariant.Hit -> MaterialTheme.colorScheme.tertiaryContainer
        variant == NumberBallVariant.Miss -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        variant == NumberBallVariant.Hit -> MaterialTheme.colorScheme.tertiary
        variant == NumberBallVariant.Miss -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .semantics {
                contentDescription = "$number"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            number.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = textColor
        )
    }
}
