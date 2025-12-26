package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUnpinnedGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): Flow<ImmutableList<LotofacilGame>> {
        return gameRepository.unpinnedGames
    }
}
