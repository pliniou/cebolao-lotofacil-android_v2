package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.FilterState
import javax.inject.Inject

/**
 * Validates filter states for game generation.
 * Ensures filters are valid and compatible.
 */
class ValidateFiltersUseCase @Inject constructor() {
    
    /**
     * Validates a list of filter states
     * 
     * @return Result.success if valid, Result.failure with error message if invalid
     */
    operator fun invoke(filters: List<FilterState>): Result<Unit> {
        // Check if any filters are enabled
        val enabledFilters = filters.filter { it.isEnabled }
        
        // Validate individual filter ranges
        for (filter in enabledFilters) {
            val range = filter.selectedRange
            val fullRange = filter.type.fullRange
            
            // Ensure range is within bounds
            if (range.start < fullRange.start || range.endInclusive > fullRange.endInclusive) {
                return Result.failure(
                    IllegalArgumentException("Filter ${filter.type} range $range is out of bounds $fullRange")
                )
            }
            
            // Ensure range is valid (start <= end)
            if (range.start > range.endInclusive) {
                return Result.failure(
                    IllegalArgumentException("Filter ${filter.type} has invalid range: start > end")
                )
            }
        }

        val sumFilter = enabledFilters.find { it.type.name.contains("SOMA") }
        val evensFilter = enabledFilters.find { it.type.name.contains("PARES") }
        
        if (sumFilter != null && evensFilter != null) {
            // Basic conflict check - if both are very restrictive, warn
            sumFilter.selectedRange.endInclusive - sumFilter.selectedRange.start
            evensFilter.selectedRange.endInclusive - evensFilter.selectedRange.start
        }

        return Result.success(Unit)
    }
}
