package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.ui.theme.Alpha
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Chip customizado com estilo moderno.
 * 
 * @param selected Se o chip está selecionado
 * @param onClick Callback de clique
 * @param label Texto do chip
 * @param modifier Modifier para o chip
 * @param enabled Se o chip está habilitado
 */
@Composable
fun CustomChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        modifier = modifier
            .height(Dimen.SmallButtonHeight)
            .selectedScale(selected),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colorScheme.primary,
            selectedLabelColor = colorScheme.onPrimary,
            containerColor = colorScheme.surfaceContainerHigh,
            labelColor = colorScheme.onSurfaceVariant,
            disabledContainerColor = colorScheme.surfaceContainerHigh.copy(alpha = Alpha.DISABLED),
            disabledLabelColor = colorScheme.onSurfaceVariant.copy(alpha = Alpha.DISABLED)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = colorScheme.outlineVariant.copy(alpha = 0f),
            selectedBorderColor = colorScheme.outlineVariant.copy(alpha = 0f)
        )
    )
}
