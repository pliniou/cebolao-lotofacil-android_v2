package com.cebolao.lotofacil.ui.components.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyDisplay
import com.cebolao.lotofacil.ui.theme.filterIcon
import com.cebolao.lotofacil.util.Formatters

@Composable
fun FilterCard(
    state: FilterState,
    onToggle: (Boolean) -> Unit,
    onRange: (ClosedFloatingPointRange<Float>) -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
    lastDraw: Set<Int>? = null
) {
    val steps = (state.type.fullRange.endInclusive - state.type.fullRange.start).toInt() - 1
    val scheme = MaterialTheme.colorScheme

    com.cebolao.lotofacil.ui.components.layout.AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { onToggle(!state.isEnabled) },
        outlined = true,
        color = if (state.isEnabled) scheme.surface else scheme.surfaceContainer,
        contentPadding = Dimen.Spacing16
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon
                    Surface(
                        color = if (state.isEnabled) scheme.primaryContainer else scheme.surfaceContainerHighest,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimen.CornerRadiusSmall),
                        modifier = Modifier.size(Dimen.IconLarge)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                           Icon(
                               imageVector = state.type.filterIcon,
                               contentDescription = null,
                               tint = if (state.isEnabled) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                               modifier = Modifier.size(Dimen.IconSmall)
                           )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(Dimen.Spacing12))
                    
                    Text(
                        text = stringResource(state.type.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isEnabled) scheme.onSurface else scheme.onSurface.copy(alpha = 0.6f)
                    )

                    IconButton(onClick = onInfo) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(
                                R.string.filter_info_button_description,
                                stringResource(state.type.titleRes)
                            ),
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(Dimen.IconSmall)
                        )
                    }
                }
                
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.scale(0.8f) 
                )
            }
            
            // Content Row
            if (state.isEnabled) {
                FilterControls(state, onRange, steps)
            }
        }
    }
}

@Composable
private fun FilterControls(
    state: FilterState, 
    onRange: (ClosedFloatingPointRange<Float>) -> Unit,
    steps: Int
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(top = Dimen.Spacing8)) {
        // Range Values
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.Spacing4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.filters_current_range),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = "${state.selectedRange.start.toInt()} - ${state.selectedRange.endInclusive.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )
            }
            
            val coverage = calculateRangeCoverage(state)
            // Coverage Badge
            Surface(
                color = scheme.secondaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimen.CornerRadiusSmall)
            ) {
                Text(
                    text = "${Formatters.formatPercentage(coverage)} ${stringResource(R.string.filters_coverage)}", 
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = Dimen.Spacing8, vertical = Dimen.Spacing4)
                )
            }
        }

        RangeSlider(
            value = state.selectedRange,
            onValueChange = onRange,
            valueRange = state.type.fullRange,
            steps = steps,
            modifier = Modifier.padding(top = Dimen.Spacing4, start = Dimen.Spacing4, end = Dimen.Spacing4),
        )

        RangeInfoSection(state)
    }
}

@Composable
private fun RangeInfoSection(state: FilterState) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth().padding(top = Dimen.Spacing4)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.Spacing4),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
             RangeIndicator(
                 icon = Icons.Default.ArrowDownward, 
                 text = "${stringResource(R.string.filters_min_label)}: ${state.type.fullRange.start.toInt()}", 
                 color = scheme.onSurfaceVariant
             )
             RangeIndicator(
                 icon = Icons.Default.ArrowUpward, 
                 text = "${stringResource(R.string.filters_max_label)}: ${state.type.fullRange.endInclusive.toInt()}", 
                 color = scheme.onSurfaceVariant
             )
        }
        
        // Recommended Row
        Text(
            text = stringResource(
                R.string.filter_recommended_range, 
                state.type.defaultRange.start.toInt(), 
                state.type.defaultRange.endInclusive.toInt()
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = scheme.secondary,
            modifier = Modifier
                .padding(top = Dimen.Spacing4, start = Dimen.Spacing4)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun RangeIndicator(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimen.IconSmall),
            tint = color
        )
        Spacer(modifier = Modifier.width(Dimen.Spacing4))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun calculateRangeCoverage(state: FilterState): Float {
    val currentRange = state.selectedRange.endInclusive - state.selectedRange.start
    val totalRange = state.type.fullRange.endInclusive - state.type.fullRange.start
    return if (totalRange > 0) (currentRange / totalRange * 100f) else 0f
}

@Preview(showBackground = true)
@Composable
private fun FilterCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(Dimen.Spacing16)) {
            FilterCard(
                state = FilterState(
                    type = FilterType.PARES,
                    isEnabled = true,
                    selectedRange = 6f..9f
                ),
                onToggle = {},
                onRange = {},
                onInfo = {},
                lastDraw = null
            )
        }
    }
}
