package com.cebolao.lotofacil.domain.usecase


import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.util.toAppError
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class SaveGeneratedGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(games: List<LotofacilGame>): AppResult<Unit> = withContext(ioDispatcher) {
        try {
            gameRepository.addGeneratedGames(games)
            AppResult.Success(Unit)
        } catch (e: IOException) {
            AppResult.Failure(e.toAppError())
        } catch (e: Exception) {
            AppResult.Failure(e.toAppError())
        }
    }
}
