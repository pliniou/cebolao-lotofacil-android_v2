package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.util.toAppError
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.DrawWindow
import com.cebolao.lotofacil.domain.model.FinancialCalculator
import com.cebolao.lotofacil.domain.model.Hit
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameCheckEngine
import com.cebolao.lotofacil.domain.service.TicketValidator
import com.cebolao.lotofacil.domain.util.Logger
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
    private val logger: Logger,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(gameNumbers: Set<Int>): Flow<AppResult<CheckReport>> = flow {
        val result: AppResult<CheckReport> = try {
            // Validar ticket antes de processar
            when (val validation = ticketValidator.validate(gameNumbers)) {
                is com.cebolao.lotofacil.domain.service.ValidationResult.Error -> {
                    AppResult.Failure(AppError.Validation(validation.message))
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
                    
                    // Calcular métricas financeiras com validação de dados históricos
                    val financialMetrics = if (history.isNotEmpty()) {
                        FinancialCalculator.calculate(
                            checkResult = checkResult,
                            totalDraws = history.size,
                            gameCost = GameConstants.GAME_COST,
                            historicalData = history
                        )
                    } else {
                        FinancialCalculator.calculate(
                            checkResult = checkResult,
                            totalDraws = 0,
                            gameCost = GameConstants.GAME_COST
                        )
                    }
                    
                    val timestamp = System.currentTimeMillis()
                    
                    val report = CheckReport(
                        ticket = game,
                        drawWindow = drawWindow,
                        hits = hits,
                        financialMetrics = financialMetrics,
                        timestamp = timestamp,
                        sourceHash = sourceHash
                    )
                    
                    AppResult.Success(report)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            logger.error(TAG, "Analysis failed", e)
            AppResult.Failure(e.toAppError())
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
