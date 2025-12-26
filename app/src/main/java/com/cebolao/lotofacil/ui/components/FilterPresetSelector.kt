package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.data.FilterPresets
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Motion

@Composable
fun FilterPresetSelector(
    onPresetSelected: (FilterPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedPresetLabelRes by rememberSaveable { mutableStateOf<Int?>(null) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = Motion.Tween.fast(),
        label = "presetChevronRotation"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            outlined = true,
            color = scheme.surface,
            contentPadding = Dimen.CardContentPadding,
            onClick = { expanded = true }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
                ) {
                    Surface(
                        color = scheme.secondaryContainer,
                        contentColor = scheme.onSecondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(Dimen.Spacing8)
                                .size(Dimen.IconMedium)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)) {
                        Text(
                            text = stringResource(R.string.preset_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedPresetLabelRes?.let { stringResource(it) }
                                ?: stringResource(R.string.preset_none),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.medium)
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                containerColor = scheme.surfaceContainerHigh,
                tonalElevation = Dimen.Elevation.Level2,
                shadowElevation = Dimen.Elevation.Level2
            ) {
                FilterPresets.all.forEach { preset ->
                    val isSelected = selectedPresetLabelRes == preset.labelRes

                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(preset.labelRes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) scheme.primary else scheme.onSurface
                                )
                                Text(
                                    text = stringResource(preset.descriptionRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
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
                        },
                        onClick = {
                            selectedPresetLabelRes = preset.labelRes
                            onPresetSelected(preset)
                            expanded = false
                        },
                        contentPadding = PaddingValues(
                            horizontal = Dimen.Spacing16,
                            vertical = Dimen.Spacing8
                        )
                    )
                }
            }
        }
    }
}
