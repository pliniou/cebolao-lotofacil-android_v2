package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import javax.inject.Inject

class ToggleGamePinUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {

    suspend operator fun invoke(game: LotofacilGame): AppResult<Unit> {
        return when (val existingResult = gameRepository.getGame(game.mask)) {
            is AppResult.Failure -> existingResult
            is AppResult.Success -> {
                val existingGame = existingResult.value
                val updatedGame = if (existingGame != null) {
                    existingGame.copy(isPinned = !existingGame.isPinned)
                } else {
                    // Se o jogo nao existe, cria um novo com o estado invertido do atual.
                    game.copy(isPinned = !game.isPinned)
                }
                gameRepository.saveGame(updatedGame)
            }
        }
    }
}
