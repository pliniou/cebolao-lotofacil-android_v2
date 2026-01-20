package com.cebolao.lotofacil.domain.usecase


import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.util.toAppError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class ToggleGamePinUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(game: LotofacilGame): AppResult<Unit> = withContext(ioDispatcher) {
        try {
            val existingGame = gameRepository.getGame(game.mask)

            if (existingGame != null) {
                val updatedGame = existingGame.copy(isPinned = !existingGame.isPinned)
                gameRepository.saveGame(updatedGame)
            } else {
                // Se o jogo não existe, cria um novo com o estado invertido do atual (que provavelmente é false)
                val newGame = game.copy(isPinned = !game.isPinned)
                gameRepository.saveGame(newGame)
            }
            AppResult.Success(Unit)
        } catch (e: IOException) {
            AppResult.Failure(e.toAppError())
        } catch (e: Exception) {
            AppResult.Failure(e.toAppError())
        }
    }
}
