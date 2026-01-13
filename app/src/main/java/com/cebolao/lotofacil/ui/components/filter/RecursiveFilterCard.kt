package com.cebolao.lotofacil.ui.components.filter

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
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Componente recursivo para organização de filtros hierárquicos.
 * Aplica design flat consistente com espaçamentos otimizados.
 */
@Composable
fun RecursiveFilterCard(
    title: String,
    filters: List<FilterItem>,
    modifier: Modifier = Modifier,
    nestingLevel: Int = 0
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = Dimen.Spacing16
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            filters.forEach { filter ->
                FilterItemRow(
                    item = filter,
                    nestingLevel = nestingLevel
                )
            }
        }
    }
}

/**
 * Componente recursivo para itens de filtro com suporte a sub-filtros.
 */
@Composable
private fun FilterItemRow(
    item: FilterItem,
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
            text = item.status,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
    
    // Recursividade para sub-filtros
    if (item.subItems.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4),
            modifier = Modifier.padding(top = Dimen.Spacing8)
        ) {
            item.subItems.forEach { subItem ->
                FilterItemRow(
                    item = subItem,
                    nestingLevel = nestingLevel + 1
                )
            }
        }
    }
}

/**
 * Data class para itens de filtro com suporte recursivo.
 */
data class FilterItem(
    val label: String,
    val status: String,
    val subItems: List<FilterItem> = emptyList()
) {
    companion object {
        fun createRecursive(
            label: String,
            status: String,
            vararg subItems: FilterItem
        ) = FilterItem(label, status, subItems.toList())
        
        fun fromPair(label: String, status: String) = FilterItem(label, status)
        
        fun active(label: String) = FilterItem(label, "Ativo")
        fun inactive(label: String) = FilterItem(label, "Inativo")
        fun custom(label: String, value: String) = FilterItem(label, value)
    }
}

/**
 * Utilitário para criar listas de filtros recursivas.
 */
object FilterListBuilder {
    fun buildFilterGroup(
        title: String,
        vararg pairs: Pair<String, String>
    ): List<FilterItem> {
        return pairs.map { (label, status) -> FilterItem.fromPair(label, status) }
    }
    
    fun buildNestedFilter(
        mainLabel: String,
        mainStatus: String,
        subFilters: List<FilterItem>
    ): FilterItem {
        return FilterItem(mainLabel, mainStatus, subFilters)
    }
    
    fun buildPresetFilters(
        presets: List<String>,
        activeIndex: Int
    ): List<FilterItem> {
        return presets.mapIndexed { index, preset ->
            FilterItem(
                label = preset,
                status = if (index == activeIndex) "Ativo" else "Inativo"
            )
        }
    }
}
