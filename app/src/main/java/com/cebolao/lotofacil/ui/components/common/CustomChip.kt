package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            .height(Dimen.SmallButtonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colorScheme.primary,
            selectedLabelColor = colorScheme.onPrimary,
            containerColor = colorScheme.secondaryContainer.copy(alpha = 0.5f), // Even more subtle accent
            labelColor = colorScheme.onSecondaryContainer,
            disabledContainerColor = colorScheme.surfaceContainerHigh.copy(alpha = Alpha.DISABLED),
            disabledLabelColor = colorScheme.onSurfaceVariant.copy(alpha = Alpha.DISABLED)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = colorScheme.outlineVariant,
            selectedBorderColor = colorScheme.primary.copy(alpha = 0.1f)
        )
    )
}

@Preview
@Composable
internal fun CustomChipPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(Dimen.Spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing8)
        ) {
            CustomChip(selected = true, onClick = {}, label = "Selecionado")
            CustomChip(selected = false, onClick = {}, label = "Não Selecionado")
            CustomChip(selected = false, onClick = {}, label = "Desabilitado", enabled = false)
        }
    }
}
