package com.cebolao.lotofacil.ui.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.components.layout.AppCard
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Data class para itens estatísticos com suporte recursivo.
 */
public data class StatItem(
    val label: String,
    val value: String,
    val subItems: List<StatItem> = emptyList()
) {
    public companion object {
        public fun createRecursive(
            label: String,
            value: String,
            vararg subItems: StatItem
        ) = StatItem(label, value, subItems.toList())
        
        public fun fromPair(label: String, value: String) = StatItem(label, value)
    }
}

/**
 * Componente recursivo para exibir estatísticas hierárquicas.
 * Suporta aninhamento automático com indentação progressiva.
 */
@Composable
fun RecursiveStatsCard(
    title: String,
    stats: List<StatItem>,
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
            
            if (stats.size > 20) {
                // For large stat lists we flatten the recursive structure into a simple list of
                // (StatItem, nestingLevel) pairs and render it with a single LazyColumn. This
                // avoids deep nested Columns/LazyColumns and keeps composition and measurement
                // efficient for big datasets.
                val flattened = remember(stats) {
                    val list = mutableListOf<Pair<StatItem, Int>>()
                    fun flatten(items: List<StatItem>, level: Int) {
                        for (it in items) {
                            list.add(it to level)
                            if (it.subItems.isNotEmpty()) flatten(it.subItems, level + 1)
                        }
                    }
                    flatten(stats, nestingLevel)
                    list.toList()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimen.ItemSpacing)
                ) {
                    items(flattened, key = { (stat, level) -> stat.label + stat.value + level }) { (stat, level) ->
                        StatItemRow(item = stat, nestingLevel = level)
                    }
                }
            } else {
                stats.forEach { stat ->
                    StatItemRow(item = stat, nestingLevel = nestingLevel)
                }
            }
        }
    }
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

/**
 * Utilitário para criar listas de estatísticas recursivas.
 */
object StatListBuilder {
    fun buildStats(vararg pairs: Pair<String, String>): List<StatItem> {
        return pairs.map { (label, value) -> StatItem.fromPair(label, value) }
    }
    
    fun buildNestedStats(
        mainLabel: String,
        mainValue: String,
        subStats: List<StatItem>
    ): StatItem {
        return StatItem(mainLabel, mainValue, subStats)
    }
}
