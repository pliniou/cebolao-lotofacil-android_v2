package com.cebolao.lotofacil.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Card recursivo para exibição de informações em formato de lista.
 * Aplica design flat consistente com espaçamentos otimizados.
 */
@Composable
fun RecursiveInfoCard(
    items: List<InfoItem>,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    AppCard(
        modifier = modifier,
        title = title,
        contentPadding = Dimen.Spacing16
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            items.forEach { item ->
                InfoItemRow(item = item)
            }
        }
    }
}

/**
 * Componente recursivo para itens de informação.
 * Suporta aninhamento automático com sub-itens.
 */
@Composable
private fun InfoItemRow(
    item: InfoItem,
    nestingLevel: Int = 0
) {
    val horizontalPadding = Dimen.Spacing8 + (nestingLevel * Dimen.Spacing8.value).dp
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween
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
    
    // Recursividade para sub-itens
    if (item.subItems.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4),
            modifier = Modifier.padding(top = Dimen.Spacing8)
        ) {
            item.subItems.forEach { subItem ->
                InfoItemRow(item = subItem, nestingLevel = nestingLevel + 1)
            }
        }
    }
}

/**
 * Data class para itens de informação com suporte recursivo.
 */
data class InfoItem(
    val label: String,
    val value: String,
    val subItems: List<InfoItem> = emptyList()
) {
    companion object {
        fun createRecursive(
            label: String,
            value: String,
            vararg subItems: InfoItem
        ) = InfoItem(label, value, subItems.toList())
    }
}
