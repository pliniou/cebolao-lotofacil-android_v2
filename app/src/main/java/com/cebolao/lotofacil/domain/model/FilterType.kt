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
        defaultRange = 170f..220f,
        historicalSuccessRate = 0.62f
    ),

    PARES(
        titleRes = R.string.filter_pares_title,
        descriptionRes = R.string.filter_pares_desc,
        fullRange = 0f..12f,
        defaultRange = 6f..9f,
        historicalSuccessRate = 0.68f
    ),

    PRIMOS(
        titleRes = R.string.filter_primos_title,
        descriptionRes = R.string.filter_primos_desc,
        fullRange = 0f..9f,
        defaultRange = 4f..7f,
        historicalSuccessRate = 0.64f
    ),

    MOLDURA(
        titleRes = R.string.filter_moldura_title,
        descriptionRes = R.string.filter_moldura_desc,
        fullRange = 0f..15f,
        defaultRange = 8f..11f,
        historicalSuccessRate = 0.66f
    ),

    FIBONACCI(
        titleRes = R.string.filter_fibonacci_title,
        descriptionRes = R.string.filter_fibonacci_desc,
        fullRange = 0f..7f,
        defaultRange = 3f..5f,
        historicalSuccessRate = 0.58f
    ),



    REPETIDAS_CONCURSO_ANTERIOR(
        titleRes = R.string.filter_repetidas_title,
        descriptionRes = R.string.filter_repetidas_desc,
        fullRange = 0f..15f,
        defaultRange = 8f..10f,
        historicalSuccessRate = 0.74f
    ),

    SEQUENCIAS(
        titleRes = R.string.filter_sequencias_title,
        descriptionRes = R.string.filter_sequencias_desc,
        fullRange = 0f..5f,
        defaultRange = 0f..2f,
        historicalSuccessRate = 0.72f
    ),

    LINHAS(
        titleRes = R.string.filter_linhas_title,
        descriptionRes = R.string.filter_linhas_desc,
        fullRange = 0f..5f,
        defaultRange = 2f..4f,
        historicalSuccessRate = 0.65f
    ),

    COLUNAS(
        titleRes = R.string.filter_colunas_title,
        descriptionRes = R.string.filter_colunas_desc,
        fullRange = 0f..5f,
        defaultRange = 2f..4f,
        historicalSuccessRate = 0.63f
    ),

    QUADRANTES(
        titleRes = R.string.filter_quadrantes_title,
        descriptionRes = R.string.filter_quadrantes_desc,
        // Agora representa quantos quadrantes (0..4) estão presentes no jogo
        fullRange = 0f..4f,
        defaultRange = 2f..3f,
        historicalSuccessRate = 0.70f
    );
}
