package com.cebolao.lotofacil.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class FilterType(
    val fullRange: ClosedFloatingPointRange<Float>,
    val defaultRange: ClosedFloatingPointRange<Float>
) {
    SOMA_DEZENAS(
        fullRange = 120f..270f,
        defaultRange = 160f..230f
    ),

    PARES(
        fullRange = 0f..15f,
        defaultRange = 6f..9f
    ),

    PRIMOS(
        fullRange = 0f..15f,
        defaultRange = 3f..7f
    ),

    MOLDURA(
        fullRange = 0f..16f,
        defaultRange = 7f..12f
    ),

    CENTER(
        fullRange = 0f..9f,
        defaultRange = 2f..7f
    ),

    FIBONACCI(
        fullRange = 0f..15f,
        defaultRange = 3f..6f
    ),

    MULTIPLES_OF_3(
        fullRange = 0f..15f,
        defaultRange = 3f..7f
    ),

    REPETIDAS_CONCURSO_ANTERIOR(
        fullRange = 0f..15f,
        defaultRange = 7f..11f
    ),

    SEQUENCIAS(
        fullRange = 0f..8f,
        defaultRange = 1f..3f
    );
    
    companion object {
        fun defaults(): List<FilterState> {
            return entries.map { type ->
                FilterState(
                    type = type,
                    isEnabled = false,
                    selectedRange = type.defaultRange
                )
            }
        }
    }
}
