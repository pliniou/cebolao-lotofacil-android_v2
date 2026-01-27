package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import javax.inject.Inject

class SaveGeneratedGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(games: List<LotofacilGame>): AppResult<Unit> =
        gameRepository.addGeneratedGames(games)
}
