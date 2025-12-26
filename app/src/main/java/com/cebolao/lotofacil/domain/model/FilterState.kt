package com.cebolao.lotofacil.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Serializable
data class FilterState(
    val type: FilterType,
    val isEnabled: Boolean = false,
    val selectedRange: ClosedFloatingPointRange<Float> = type.defaultRange
) {
    val rangePercentage: Float
        get() {
            val total = abs(type.fullRange.endInclusive - type.fullRange.start)
            if (total == 0f) return 1f

            val current = abs(selectedRange.endInclusive - selectedRange.start)
            return (current / total).coerceIn(0f, 1f)
        }

    fun containsValue(value: Int): Boolean {
        val start = min(selectedRange.start, selectedRange.endInclusive)
        val end = max(selectedRange.start, selectedRange.endInclusive)
        return value.toFloat() in start..end
    }
}
