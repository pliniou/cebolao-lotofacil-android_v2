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
        try {
            // 1. Validate Ticket
            val validation = ticketValidator.validate(gameNumbers)
            if (validation is com.cebolao.lotofacil.domain.service.ValidationResult.Error) {
                emit(AppResult.Failure(AppError.Validation(validation.message)))
                return@flow
            }

            // 2. Garantir histÇürico atualizado antes de calcular
            val syncResult = historyRepository.syncHistoryIfNeeded()
            if (syncResult.isFailure) {
                logger.warning(TAG, "SincronizaÇõÇœo falhou ou nÇõo necessÇ­ria; usando cache local")
            }

            // 3. Fetch History
            val history = historyRepository.getHistory()
            if (history.isEmpty()) {
                logger.warning(TAG, "History is empty during check")
                // Ideally return a specific error or proceed with empty stats
            }

            // 4. Prepare Data
            val game = LotofacilGame.fromNumbers(gameNumbers)
            val sourceHash = calculateTicketHash(game)
            
            // 5. Delegate to Engine
            val checkResult = gameCheckEngine.checkGame(game, history)
            
            // 6. Build Report
            val drawWindow = DrawWindow(
                firstContest = history.lastOrNull()?.contestNumber ?: 0,
                lastContest = history.firstOrNull()?.contestNumber ?: 0,
                totalDraws = history.size
            )
            
            val hits = checkResult.recentHits.map { (contestNumber, score) ->
                Hit(contestNumber = contestNumber, score = score)
            }
            
            val financialMetrics = FinancialCalculator.calculate(
                checkResult = checkResult,
                totalDraws = history.size,
                gameCost = GameConstants.GAME_COST,
                historicalData = history
            )
            
            val report = CheckReport(
                ticket = game,
                drawWindow = drawWindow,
                hits = hits,
                financialMetrics = financialMetrics,
                timestamp = System.currentTimeMillis(),
                sourceHash = sourceHash
            )
            
            emit(AppResult.Success(report))

        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            logger.error(TAG, "Analysis failed", e)
            emit(AppResult.Failure(e.toAppError()))
        }
    }.flowOn(defaultDispatcher)
    
    // Hash calculation kept internal for now as moving to Model requires viewing Model first
    private fun calculateTicketHash(game: LotofacilGame): String {
        val numbers = game.numbers.sorted().joinToString(",")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(numbers.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
