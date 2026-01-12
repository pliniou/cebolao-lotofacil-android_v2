package com.cebolao.lotofacil.domain.model

import androidx.annotation.StringRes
import com.cebolao.lotofacil.R
import kotlinx.serialization.Serializable

@Serializable
enum class FilterType(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val fullRange: ClosedFloatingPointRange<Float>,
    val defaultRange: ClosedFloatingPointRange<Float>,
    val historicalSuccessRate: Float
) {
    SOMA_DEZENAS(
        titleRes = R.string.filter_soma_title,
        descriptionRes = R.string.filter_soma_desc,
        fullRange = 120f..270f,
        defaultRange = 160f..230f,
        historicalSuccessRate = 0.62f
    ),

    PARES(
        titleRes = R.string.filter_pares_title,
        descriptionRes = R.string.filter_pares_desc,
        fullRange = 0f..15f,
        defaultRange = 6f..9f,
        historicalSuccessRate = 0.68f
    ),

    PRIMOS(
        titleRes = R.string.filter_primos_title,
        descriptionRes = R.string.filter_primos_desc,
        fullRange = 0f..15f,
        defaultRange = 3f..7f,
        historicalSuccessRate = 0.64f
    ),

    MOLDURA(
        titleRes = R.string.filter_moldura_title,
        descriptionRes = R.string.filter_moldura_desc,
        fullRange = 0f..16f,
        defaultRange = 7f..12f,
        historicalSuccessRate = 0.66f
    ),

    CENTER(
        titleRes = R.string.filter_retrato_title,
        descriptionRes = R.string.filter_retrato_desc,
        fullRange = 0f..9f,
        defaultRange = 2f..7f,
        historicalSuccessRate = 0.65f
    ),

    FIBONACCI(
        titleRes = R.string.filter_fibonacci_title,
        descriptionRes = R.string.filter_fibonacci_desc,
        fullRange = 0f..15f,
        defaultRange = 3f..6f,
        historicalSuccessRate = 0.58f
    ),

    MULTIPLES_OF_3(
        titleRes = R.string.filter_multiplos3_title,
        descriptionRes = R.string.filter_multiplos3_desc,
        fullRange = 0f..15f,
        defaultRange = 3f..7f,
        historicalSuccessRate = 0.60f
    ),

    REPETIDAS_CONCURSO_ANTERIOR(
        titleRes = R.string.filter_repetidas_title,
        descriptionRes = R.string.filter_repetidas_desc,
        fullRange = 0f..15f,
        defaultRange = 7f..11f,
        historicalSuccessRate = 0.74f
    ),

    SEQUENCIAS(
        titleRes = R.string.filter_sequencias_title,
        descriptionRes = R.string.filter_sequencias_desc,
        fullRange = 0f..8f,
        defaultRange = 1f..3f,
        historicalSuccessRate = 0.72f
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
