package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.DrawDetails
import com.cebolao.lotofacil.domain.model.GameAnalysisResult
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.service.GameCheckEngine
import com.cebolao.lotofacil.domain.service.TicketValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeGameUseCaseTest {

    private class FakeHistoryRepository(private val history: List<Draw>) : com.cebolao.lotofacil.domain.repository.HistoryRepository {
        override val syncStatus = kotlinx.coroutines.flow.flowOf<com.cebolao.lotofacil.domain.repository.SyncStatus>(com.cebolao.lotofacil.domain.repository.SyncStatus.Idle).let { kotlinx.coroutines.flow.MutableStateFlow(com.cebolao.lotofacil.domain.repository.SyncStatus.Idle) }
        override fun syncHistory(): kotlinx.coroutines.Job = kotlinx.coroutines.Job()
        override fun observeHistory(): kotlinx.coroutines.flow.Flow<List<Draw>> = flow { emit(history) }
        override fun observeLastDraw(): kotlinx.coroutines.flow.Flow<Draw?> = flow { emit(history.firstOrNull()) }
        override suspend fun getHistory(): List<Draw> = history
        override suspend fun getLastDraw(): Draw? = history.firstOrNull()
        override suspend fun getLastDrawDetails(): DrawDetails? = null
    }

    @Test
    fun analyzeGame_returnsDomainGameAnalysisResult() = runBlocking {
        // arrange
        val numbers = (1..15).toSet()
        val draw = Draw.fromNumbers(contestNumber = 1, numbers = numbers)
        val fakeRepo = FakeHistoryRepository(listOf(draw))

        val checkUseCase = CheckGameUseCase(fakeRepo, GameCheckEngine(), TicketValidator(), Dispatchers.Unconfined)
        val statsUseCase = GetGameSimpleStatsUseCase()

        val sut = AnalyzeGameUseCase(checkUseCase, statsUseCase, Dispatchers.Unconfined)

        // act
        val result = sut(LotofacilGame.fromNumbers(numbers))

        // assert
        assertTrue(result.isSuccess)
        val analysis = result.getOrNull()
        assertNotNull(analysis)
        assertTrue(analysis is GameAnalysisResult)
        assertEquals(numbers, analysis!!.game.numbers)
        assertNotNull(analysis.metrics)
    }
}
