package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.DrawWindow
import com.cebolao.lotofacil.domain.model.FinancialCalculator
import com.cebolao.lotofacil.domain.model.Hit
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameCheckEngine
import com.cebolao.lotofacil.domain.service.TicketValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.security.MessageDigest
import javax.inject.Inject

private const val TAG = "CheckGameUseCase"

class CheckGameUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val gameCheckEngine: GameCheckEngine,
    private val ticketValidator: TicketValidator,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(gameNumbers: Set<Int>): Flow<Result<CheckReport>> = flow {
        val result: Result<CheckReport> = try {
            // Validar ticket antes de processar
            when (val validation = ticketValidator.validate(gameNumbers)) {
                is com.cebolao.lotofacil.domain.service.ValidationResult.Error -> {
                    Result.failure(IllegalArgumentException(validation.message))
                }
                is com.cebolao.lotofacil.domain.service.ValidationResult.Success -> {
                    val history = historyRepository.getHistory()
                    require(history.isNotEmpty()) { "History unavailable" }
                    
                    // Converter Set<Int> para LotofacilGame
                    val game = LotofacilGame.fromNumbers(gameNumbers)
                    
                    // Calcular hash SHA-256 do ticket para auditoria
                    val sourceHash = calculateTicketHash(game)
                    
                    // Delegar cálculo para GameCheckEngine
                    val checkResult = gameCheckEngine.checkGame(game, history)
                    
                    // Criar DrawWindow a partir do histórico
                    val drawWindow = DrawWindow(
                        firstContest = history.lastOrNull()?.contestNumber ?: 0,
                        lastContest = history.firstOrNull()?.contestNumber ?: 0,
                        totalDraws = history.size
                    )
                    
                    // Converter recentHits para List<Hit>
                    val hits = checkResult.recentHits.map { (contestNumber, score) ->
                        Hit(contestNumber = contestNumber, score = score)
                    }
                    
                    // Calcular métricas financeiras
                    val financialMetrics = FinancialCalculator.calculate(
                        checkResult = checkResult,
                        totalDraws = history.size,
                        gameCost = GameConstants.GAME_COST,
                        historicalData = history
                    )
                    
                    val timestamp = System.currentTimeMillis()
                    
                    val report = CheckReport(
                        ticket = game,
                        drawWindow = drawWindow,
                        hits = hits,
                        financialMetrics = financialMetrics,
                        timestamp = timestamp,
                        sourceHash = sourceHash
                    )
                    
                    Result.success(report)
                }
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Analysis failed", e)
            Result.failure(e)
        }

        emit(result)
    }.flowOn(defaultDispatcher)
    
    /**
     * Calcula hash SHA-256 do ticket para auditoria
     * Usa os números ordenados do ticket como entrada
     */
    private fun calculateTicketHash(game: LotofacilGame): String {
        val numbers = game.numbers.sorted().joinToString(",")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(numbers.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
