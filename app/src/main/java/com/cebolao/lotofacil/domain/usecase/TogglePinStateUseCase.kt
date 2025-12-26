package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TogglePinStateUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame) = withContext(ioDispatcher) {
        runCatching {
            gameRepository.togglePinState(game)
        }
    }
}
