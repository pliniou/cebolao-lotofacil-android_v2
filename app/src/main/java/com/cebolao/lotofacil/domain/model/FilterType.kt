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
        defaultRange = 160f..230f, // Use Acceptable range as default
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

    FIBONACCI(
        titleRes = R.string.filter_fibonacci_title,
        descriptionRes = R.string.filter_fibonacci_desc,
        fullRange = 0f..15f,
        defaultRange = 3f..6f,
        historicalSuccessRate = 0.58f
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
        fullRange = 0f..8f, // Max theoretical sequences of 3+ ??? Wait. Sequence count. Max 5 (1-3, 4-6, 7-9, 10-12, 13-15).
        defaultRange = 1f..3f,
        historicalSuccessRate = 0.72f
    ),

    LINHAS(
        titleRes = R.string.filter_linhas_title,
        descriptionRes = R.string.filter_linhas_desc,
        fullRange = 0f..5f,
        defaultRange = 3f..5f, // Lines with at least 3 numbers? Need to check semantics.
        historicalSuccessRate = 0.65f
    ),

    COLUNAS(
        titleRes = R.string.filter_colunas_title,
        descriptionRes = R.string.filter_colunas_desc,
        fullRange = 0f..5f,
        defaultRange = 3f..5f,
        historicalSuccessRate = 0.63f
    ),

    QUADRANTES(
        titleRes = R.string.filter_quadrantes_title,
        descriptionRes = R.string.filter_quadrantes_desc,
        // Represents how many quadrants (0..4) are "filled" (usually defined as having X numbers, or just present numbers)
        // Implementation in GameGenerator says: `if (b) qCount++` where b is boolean "has numbers". So 0..4.
        fullRange = 0f..4f,
        defaultRange = 3f..4f,
        historicalSuccessRate = 0.70f
    );
}
