package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.service.GeneratorConfig
import com.cebolao.lotofacil.domain.service.GenerationProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Encapsulates the game generation logic.  Returns a flow of generation progress wrapped in [AppResult].
 */
class GenerateGamesUseCase @Inject constructor(
    private val gameGenerator: GameGenerator
) {
    operator fun invoke(
        quantity: Int,
        filters: List<FilterState>,
        config: GeneratorConfig = GeneratorConfig.BALANCED,
        seed: Long? = null
    ): Flow<AppResult<GenerationProgress>> {
        return gameGenerator.generate(quantity, filters, config, seed)
            .map<AppResult<GenerationProgress>> { progress -> AppResult.Success(progress) }
            .catch { throwable -> emit(AppResult.Failure(AppError.Unknown(throwable))) }
    }
}
