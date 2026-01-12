package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.service.GeneratorConfig
import com.cebolao.lotofacil.domain.service.GenerationProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Encapsula a lógica de negócio para gerar jogos com base em filtros.
 * Intermedia ViewModel -> GameGenerator.
 */
class GenerateGamesUseCase @Inject constructor(
    private val gameGenerator: GameGenerator
) {
    operator fun invoke(
        quantity: Int,
        filters: List<FilterState>,
        config: GeneratorConfig = GeneratorConfig.BALANCED,
        seed: Long? = null
    ): Flow<GenerationProgress> {
        return gameGenerator.generate(quantity, filters, config, seed)
    }
}
