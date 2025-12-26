package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.ui.theme.AppIcons
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyDisplay
import com.cebolao.lotofacil.ui.theme.filterIcon
import kotlin.math.max

@Composable
fun FilterCard(
    state: FilterState,
    onToggle: (Boolean) -> Unit,
    onRange: (ClosedFloatingPointRange<Float>) -> Unit,
    onInfo: () -> Unit,
    lastDraw: Set<Int>?,
    modifier: Modifier = Modifier
) {
    val missingData by remember(state.type, lastDraw) {
        derivedStateOf {
            state.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && lastDraw == null
        }
    }

    val active = state.isEnabled && !missingData
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = com.cebolao.lotofacil.ui.theme.Motion.Spring.gentle()),
        outlined = !active,
        color = if (active) scheme.surface else scheme.surfaceContainerLow,
        contentPadding = Dimen.Spacing8
    ) {
        Column(modifier = Modifier.padding(Dimen.Spacing8)) {
            Header(
                state = state,
                active = active,
                missing = missingData,
                onInfo = onInfo,
                onToggle = onToggle
            )

            AnimatedVisibility(
                visible = active,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Content(state = state, onRange = onRange)
            }
        }
    }
}

@Composable
private fun Header(
    state: FilterState,
    active: Boolean,
    missing: Boolean,
    onInfo: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.Spacing16)
    ) {
        Icon(
            imageVector = state.type.filterIcon,
            contentDescription = null,
            tint = if (active) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.size(Dimen.IconMedium)
        )

        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(state.type.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            if (missing) {
                Text(
                    text = stringResource(R.string.filters_unavailable_data),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.error
                )
            }
        }

        IconButton(onClick = onInfo) {
            Icon(
                imageVector = AppIcons.InfoOutlined,
                contentDescription = null,
                tint = scheme.onSurfaceVariant
            )
        }

        Switch(
            checked = state.isEnabled,
            onCheckedChange = onToggle,
            enabled = !missing,
            colors = SwitchDefaults.colors(
                checkedThumbColor = scheme.onPrimary,
                checkedTrackColor = scheme.primary
            )
        )
    }
}

@Composable
private fun Content(
    state: FilterState,
    onRange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val steps = remember(state.type.fullRange) {
        val delta = (state.type.fullRange.endInclusive - state.type.fullRange.start).toInt()
        max(0, delta - 1)
    }

    Column(modifier = Modifier.padding(top = Dimen.Spacing8)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.Spacing8),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Label(
                text = stringResource(R.string.filters_min_label),
                value = state.selectedRange.start.toInt()
            )
            Label(
                text = stringResource(R.string.filters_max_label),
                value = state.selectedRange.endInclusive.toInt(),
                align = Alignment.End
            )
        }

        RangeSlider(
            value = state.selectedRange,
            onValueChange = onRange,
            valueRange = state.type.fullRange,
            steps = steps,
            modifier = Modifier.padding(top = Dimen.Spacing8)
        )
    }
}

@Composable
private fun Label(
    text: String,
    value: Int,
    align: Alignment.Horizontal = Alignment.Start
) {
    val scheme = MaterialTheme.colorScheme

    Column(horizontalAlignment = align) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamilyDisplay,
            color = scheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}
