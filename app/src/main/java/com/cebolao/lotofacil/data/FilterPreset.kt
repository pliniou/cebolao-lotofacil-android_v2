package com.cebolao.lotofacil.data

import androidx.annotation.StringRes
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.FilterType

/**
 * Representa uma estratégia pré-definida de filtros.
 *
 * Observação: este arquivo mantém apenas dados e configurações; aplicação do preset
 * deve ser feita na camada de UI/UseCase conforme a lógica atual do projeto.
 */
data class FilterPreset(
    val id: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descriptionRes: Int,
    val rules: Map<FilterType, ClosedFloatingPointRange<Float>>
)

@Suppress("unused")
object FilterPresets {

    val all: List<FilterPreset> = listOf(
        FilterPreset(
            id = "standard",
            labelRes = R.string.preset_standard,
            descriptionRes = R.string.preset_standard_desc,
            rules = mapOf(
                FilterType.SOMA_DEZENAS to 166f..220f,
                FilterType.PARES to 6f..9f,
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 8f..10f,
                FilterType.MOLDURA to 8f..11f,
                FilterType.PRIMOS to 4f..7f
            )
        ),
        FilterPreset(
            id = "balanced",
            labelRes = R.string.preset_balanced,
            descriptionRes = R.string.preset_balanced_desc,
            rules = mapOf(
                FilterType.SOMA_DEZENAS to 170f..210f,
                FilterType.PARES to 7f..8f,
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 8f..10f
            )
        ),
        FilterPreset(
            id = "math",
            labelRes = R.string.preset_math,
            descriptionRes = R.string.preset_math_desc,
            rules = mapOf(
                FilterType.PRIMOS to 5f..6f,
                FilterType.FIBONACCI to 4f..5f,
                FilterType.SOMA_DEZENAS to 180f..210f
            )
        ),
        FilterPreset(
            id = "surprise",
            labelRes = R.string.preset_surprise,
            descriptionRes = R.string.preset_surprise_desc,
            rules = mapOf(
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 9f..9f,
                FilterType.PARES to 5f..7f
            )
        ),
        FilterPreset(
            id = "aggressive",
            labelRes = R.string.preset_aggressive,
            descriptionRes = R.string.preset_aggressive_desc,
            // Focused on highly specific patterns
            rules = mapOf(
                FilterType.SOMA_DEZENAS to 190f..205f,
                FilterType.PARES to 7f..8f,
                FilterType.PRIMOS to 5f..6f,
                FilterType.SEQUENCIAS to 1f..3f
            )
        ),
        FilterPreset(
            id = "conservative",
            labelRes = R.string.preset_conservative,
            descriptionRes = R.string.preset_conservative_desc,
            rules = mapOf(
                FilterType.SOMA_DEZENAS to 160f..230f,
                FilterType.PARES to 5f..10f,
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 7f..11f,
                FilterType.MOLDURA to 7f..12f
            )
        ),
        FilterPreset(
            id = "hot_numbers",
            labelRes = R.string.preset_hot_numbers,
            descriptionRes = R.string.preset_hot_numbers_desc,
            rules = mapOf(
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 9f..11f,
                FilterType.PARES to 6f..8f,
                FilterType.SEQUENCIAS to 2f..4f
            )
        )
    )

    val byId: Map<String, FilterPreset> = all.associateBy { it.id }
    fun find(id: String): FilterPreset? = byId[id]
}
