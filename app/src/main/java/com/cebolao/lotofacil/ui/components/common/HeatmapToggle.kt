package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun HeatmapToggle(
    isHeatmapEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isHeatmapEnabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = stringResource(
                if (isHeatmapEnabled) R.string.checker_hide_frequency 
                else R.string.checker_show_frequency
            ),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(Dimen.SpacingTiny))
        Text(
            text = stringResource(
                if (isHeatmapEnabled) R.string.checker_hide_frequency 
                else R.string.checker_show_frequency
            ),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
