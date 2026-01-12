package com.cebolao.lotofacil.domain.usecase

import android.database.sqlite.SQLiteException
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.util.toAppError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class SaveGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame): AppResult<Unit> = withContext(ioDispatcher) {
        try {
            gameRepository.addGeneratedGames(listOf(game))
            AppResult.Success(Unit)
        } catch (e: IOException) {
            AppResult.Failure(e.toAppError())
        } catch (e: SQLiteException) {
            AppResult.Failure(e.toAppError())
        }
    }
}
