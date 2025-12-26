package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.GenerationUiState

@Composable
fun GenerationActionsPanel(
    state: GenerationUiState,
    onGenerate: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

    val options = GameConstants.GAME_QUANTITY_OPTIONS
    var index by rememberSaveable { mutableIntStateOf(0) }
    index = index.coerceIn(0, options.lastIndex)

    val quantity = options[index]
    val isLoading = state is GenerationUiState.Loading

    val formatter = rememberCurrencyFormatter()
    val totalCost = remember(quantity) {
        GameConstants.GAME_COST.multiply(quantity.toBigDecimal())
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth()
        ) {
            HorizontalDivider(
                thickness = Dimen.Border.Hairline,
                color = scheme.outlineVariant.copy(alpha = 0.6f)
            )

            Row(
                modifier = Modifier
                    .padding(
                        horizontal = Dimen.ScreenPadding,
                        vertical = Dimen.SpacingShort
                    )
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = scheme.surfaceContainerHigh
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = Dimen.Spacing4,
                                vertical = Dimen.Spacing4
                            )
                        ) {
                            QuantityAdjustButton(
                                icon = AppIcons.Remove,
                                enabled = index > 0 && !isLoading
                            ) {
                                index--
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }

                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                                modifier = Modifier.padding(horizontal = Dimen.SpacingMedium)
                            )

                            QuantityAdjustButton(
                                icon = AppIcons.Add,
                                enabled = index < options.lastIndex && !isLoading
                            ) {
                                index++
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }

                    Text(
                        text = formatter.format(totalCost),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
                ) {
                    if (isLoading) {
                        FilledIconButton(
                            onClick = onCancel,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = scheme.errorContainer,
                                contentColor = scheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = AppIcons.Cancel,
                                contentDescription = stringResource(R.string.general_cancel)
                            )
                        }
                    }

                    val primaryText = when {
                        !isLoading -> stringResource(R.string.filters_button_generate)
                        state.total > 0 -> stringResource(
                            R.string.filters_button_generating_progress,
                            state.progress,
                            state.total
                        )
                        else -> stringResource(state.messageRes)
                    }

                    PrimaryActionButton(
                        text = primaryText,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        isLoading = isLoading,
                        onClick = { onGenerate(quantity) },
                        icon = {
                            if (!isLoading) {
                                Icon(
                                    imageVector = AppIcons.Send,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityAdjustButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(Dimen.ActionButtonHeight),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimen.IconMedium)
        )
    }
}
