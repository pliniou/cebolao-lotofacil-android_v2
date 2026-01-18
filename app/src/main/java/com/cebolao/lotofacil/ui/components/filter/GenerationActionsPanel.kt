package com.cebolao.lotofacil.ui.components.filter

import com.cebolao.lotofacil.ui.haptics.rememberHapticFeedback
import com.cebolao.lotofacil.ui.haptics.HapticFeedbackType

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.common.PrimaryActionButton
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun GenerationActionsPanel(
    quantity: Int,
    onQuantityChanged: (Int) -> Unit,
    onGenerate: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val haptics = com.cebolao.lotofacil.ui.haptics.rememberHapticFeedback()

    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = scheme.surfaceContainerHigh), // Consistent with bottom sheet style
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.12f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort),
            modifier = Modifier.padding(Dimen.Spacing12)
        ) {
            Text(
                text = stringResource(R.string.count_short),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant
            )

            QuantitySelector(
                quantity = quantity,
                onQuantityChanged = { 
                    onQuantityChanged(it)
                    haptics.performHapticFeedback(com.cebolao.lotofacil.ui.haptics.HapticFeedbackType.LIGHT)
                },
                enabled = !isGenerating
            )

            Spacer(modifier = Modifier.height(Dimen.SpacingTiny))

            PrimaryActionButton(
                text = stringResource(R.string.filters_button_generate),
                onClick = {
                    onGenerate()
                    haptics.performHapticFeedback(com.cebolao.lotofacil.ui.haptics.HapticFeedbackType.MEDIUM)
                },
                isLoading = isGenerating,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.AutoFixHigh
            )
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChanged: (Int) -> Unit,
    enabled: Boolean
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort),
        modifier = Modifier
            .background(scheme.surface, CircleShape) // Inner background for selector
            .padding(Dimen.Spacing4) // Padding around the controls
    ) {
        QuantityButton(
            icon = AppIcons.Remove,
            onClick = { if (quantity > 1) onQuantityChanged(quantity - 1) },
            enabled = enabled && quantity > 1,
            contentDescription = stringResource(R.string.filters_quantity_decrease)
        )

        Box(
            modifier = Modifier
                .width(Dimen.ControlWidthMedium) // Slightly wider for double digits
                .height(Dimen.ControlHeightMedium),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = quantity,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> height } + fadeOut())
                    }.using(
                        androidx.compose.animation.SizeTransform(clip = false)
                    )
                },
                label = "QuantityTransition"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        QuantityButton(
            icon = AppIcons.Add,
            onClick = { if (quantity < 50) onQuantityChanged(quantity + 1) }, // Limit max quantity reasonable
            enabled = enabled && quantity < 50,
            contentDescription = stringResource(R.string.filters_quantity_increase)
        )
    }
}

@Composable
private fun QuantityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    contentDescription: String
) {
    val scheme = MaterialTheme.colorScheme
    
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            disabledContainerColor = scheme.surfaceContainerHighest,
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = Modifier.size(Dimen.ControlHeightMedium) // Consistent touch target size
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Dimen.IconSmall)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GenerationActionsPanelPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(Dimen.Spacing16)) {
            GenerationActionsPanel(
                quantity = 5,
                onQuantityChanged = {},
                onGenerate = {},
                isGenerating = false
            )
        }
    }
}
