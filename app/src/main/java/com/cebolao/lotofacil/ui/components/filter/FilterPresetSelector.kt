package com.cebolao.lotofacil.ui.components.filter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterPreset
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.components.layout.CardVariant
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun FilterPresetSelector(
    selectedPreset: FilterPreset?,
    onPresetSelected: (FilterPreset) -> Unit,
    presets: List<FilterPreset>,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            variant = CardVariant.Solid, // Changed to Solid for flat look
            color = scheme.secondaryContainer, // Use tonal color for emphasis
            contentPadding = Dimen.Spacing12,
            hasBorder = false // No border for cleaner look
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.animateContentSize(animationSpec = Motion.Spring.gentle())
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(Dimen.IconSmall)
                )

                Spacer(modifier = Modifier.width(Dimen.Spacing8))

                AnimatedContent(
                    targetState = selectedPreset,
                    transitionSpec = {
                        (fadeIn(animationSpec = Motion.Tween.enter()))
                            .togetherWith(fadeOut(animationSpec = Motion.Tween.exit()))
                    },
                    modifier = Modifier.weight(1f),
                    label = "PresetNameTransition"
                ) { preset ->
                    Text(
                        text = preset?.let { stringResource(it.labelRes) } ?: stringResource(R.string.preset_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSecondaryContainer
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(Dimen.IconSmall)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
            containerColor = scheme.surfaceContainer, // M3 container color
            tonalElevation = 0.dp, // Flat design
            shadowElevation = Dimen.Elevation.Level2 // Keeping shadow for dropdown to separate from background
        ) {
            presets.forEach { preset ->
                val isSelected = preset.id == selectedPreset?.id
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(preset.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) scheme.primary else scheme.onSurface
                        )
                    },
                    onClick = {
                        onPresetSelected(preset)
                        expanded = false
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = AppIcons.Check,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(Dimen.IconSmall)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(Dimen.IconSmall))
                        }
                    }
                )
            }
        }
    }
}
