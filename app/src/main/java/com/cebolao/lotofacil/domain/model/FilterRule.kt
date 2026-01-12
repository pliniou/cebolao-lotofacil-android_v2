package com.cebolao.lotofacil.domain.model

/** Interface que representa uma regra de filtro aplicada sobre métricas calculadas */
interface FilterRule {
    val type: FilterType
    fun matches(metrics: GameComputedMetrics): Boolean
}

/** Implementação genérica para filtros que representam um intervalo sobre um valor inteiro */
class CountRangeFilter(
    override val type: FilterType,
    private val range: IntRange,
    private val extractor: (GameComputedMetrics) -> Int
) : FilterRule {
    override fun matches(metrics: GameComputedMetrics): Boolean {
        val value = extractor(metrics)
        return value in range
    }
}

/** Conversor de FilterState para FilterRule (se ativado) */
// toRule moved to GameGenerator as private extension due to build issues
fun FilterState.toRule(): FilterRule? {
    if (!isEnabled) return null
    val r = selectedRange.start.toInt()..selectedRange.endInclusive.toInt()
    return when (type) {
        FilterType.SOMA_DEZENAS -> CountRangeFilter(type, r) { it.sum }
        FilterType.PARES -> CountRangeFilter(type, r) { it.evens }
        FilterType.PRIMOS -> CountRangeFilter(type, r) { it.primes }
        FilterType.MOLDURA -> CountRangeFilter(type, r) { it.frame }
        FilterType.FIBONACCI -> CountRangeFilter(type, r) { it.fibonacci }
        FilterType.REPETIDAS_CONCURSO_ANTERIOR -> CountRangeFilter(type, r) { it.repeated }
        FilterType.SEQUENCIAS -> CountRangeFilter(type, r) { it.sequences }
        FilterType.MULTIPLES_OF_3 -> CountRangeFilter(type, r) { it.multiplesOf3 }
        FilterType.CENTER -> CountRangeFilter(type, r) { it.center }
    }
}
