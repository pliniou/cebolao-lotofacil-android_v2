package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Data class para itens estatísticos com suporte recursivo.
 */
data class StatItem(
    val label: String,
    val value: String,
    val subItems: List<StatItem> = emptyList()
) {
    companion object
}

/**
 * Componente recursivo para itens estatísticos com suporte a sub-itens.
 */
@Composable
private fun StatItemRow(
    item: StatItem,
    nestingLevel: Int = 0
) {
    val horizontalPadding = Dimen.Spacing8 + (nestingLevel * Dimen.Spacing8.value).dp
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (nestingLevel == 0) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = item.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
    
    // Recursividade para sub-itens estatísticos
    if (item.subItems.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4),
            modifier = Modifier.padding(top = Dimen.Spacing8)
        ) {
            item.subItems.forEach { subItem ->
                StatItemRow(
                    item = subItem,
                    nestingLevel = nestingLevel + 1
                )
            }
        }
    }
}

