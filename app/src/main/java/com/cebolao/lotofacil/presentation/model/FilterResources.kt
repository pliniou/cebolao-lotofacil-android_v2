package com.cebolao.lotofacil.presentation.model

import androidx.annotation.StringRes
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterPreset
import com.cebolao.lotofacil.domain.model.FilterType

@get:StringRes
val FilterType.titleRes: Int
    get() = when (this) {
        FilterType.SOMA_DEZENAS -> R.string.filter_soma_title
        FilterType.PARES -> R.string.filter_pares_title
        FilterType.PRIMOS -> R.string.filter_primos_title
        FilterType.MOLDURA -> R.string.filter_moldura_title
        FilterType.CENTER -> R.string.filter_retrato_title
        FilterType.FIBONACCI -> R.string.filter_fibonacci_title
        FilterType.MULTIPLES_OF_3 -> R.string.filter_multiplos3_title
        FilterType.REPETIDAS_CONCURSO_ANTERIOR -> R.string.filter_repetidas_title
        FilterType.SEQUENCIAS -> R.string.filter_sequencias_title
    }

@get:StringRes
val FilterType.descriptionRes: Int
    get() = when (this) {
        FilterType.SOMA_DEZENAS -> R.string.filter_soma_desc
        FilterType.PARES -> R.string.filter_pares_desc
        FilterType.PRIMOS -> R.string.filter_primos_desc
        FilterType.MOLDURA -> R.string.filter_moldura_desc
        FilterType.CENTER -> R.string.filter_retrato_desc
        FilterType.FIBONACCI -> R.string.filter_fibonacci_desc
        FilterType.MULTIPLES_OF_3 -> R.string.filter_multiplos3_desc
        FilterType.REPETIDAS_CONCURSO_ANTERIOR -> R.string.filter_repetidas_desc
        FilterType.SEQUENCIAS -> R.string.filter_sequencias_desc
    }

@get:StringRes
val FilterPreset.labelRes: Int
    get() = when (id) {
        "standard" -> R.string.preset_standard
        "balanced" -> R.string.preset_balanced
        "math" -> R.string.preset_math
        "surprise" -> R.string.preset_surprise
        "aggressive" -> R.string.preset_aggressive
        "conservative" -> R.string.preset_conservative
        "hot_numbers" -> R.string.preset_hot_numbers
        else -> R.string.preset_label
    }

@get:StringRes
val FilterPreset.descriptionRes: Int
    get() = when (id) {
        "standard" -> R.string.preset_standard_desc
        "balanced" -> R.string.preset_balanced_desc
        "math" -> R.string.preset_math_desc
        "surprise" -> R.string.preset_surprise_desc
        "aggressive" -> R.string.preset_aggressive_desc
        "conservative" -> R.string.preset_conservative_desc
        "hot_numbers" -> R.string.preset_hot_numbers_desc
        else -> R.string.preset_standard_desc
    }
