package com.cebolao.lotofacil.ui.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.ui.components.layout.SectionHeader
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun FilterGroupColumn(
    title: String,
    filters: List<FilterState>,
    lastDraw: Set<Int>?,
    onToggle: (FilterType, Boolean) -> Unit,
    onRangeAdjustment: (FilterType, ClosedFloatingPointRange<Float>) -> Unit,
    onInfoRequest: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimen.Spacing4)) {
        SectionHeader(title)
        filters.forEach { filter ->
            FilterCard(
                state = filter,
                onToggle = { onToggle(filter.type, it) },
                onRange = { onRangeAdjustment(filter.type, it) },
                onInfo = { onInfoRequest(filter.type) },
                lastDraw = lastDraw
            )
        }
    }
}

fun LazyListScope.filterSection(
    title: String,
    filters: List<FilterState>,
    lastDraw: Set<Int>?,
    onToggle: (FilterType, Boolean) -> Unit,
    onRangeAdjustment: (FilterType, ClosedFloatingPointRange<Float>) -> Unit,
    onInfoRequest: (FilterType) -> Unit
) {
    item(key = "header_$title") {
        SectionHeader(title, modifier = Modifier.padding(top = Dimen.Spacing4))
    }

    items(
        items = filters,
        key = { it.type.name }
    ) { filter ->
        FilterCard(
            state = filter,
            onToggle = { onToggle(filter.type, it) },
            onRange = { onRangeAdjustment(filter.type, it) },
            onInfo = { onInfoRequest(filter.type) },
            lastDraw = lastDraw,
            modifier = Modifier.padding(vertical = Dimen.Spacing4)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterGroupColumnPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            FilterGroupColumn(
                title = "Example Group",
                filters = listOf(
                    FilterState(
                        type = FilterType.PARES,
                        isEnabled = true,
                        selectedRange = 6f..9f
                    ),
                    FilterState(
                        type = FilterType.PRIMOS,
                        isEnabled = false,
                        selectedRange = 4f..6f
                    )
                ),
                lastDraw = null,
                onToggle = { _, _ -> },
                onRangeAdjustment = { _, _ -> },
                onInfoRequest = {}
            )
        }
    }
}
