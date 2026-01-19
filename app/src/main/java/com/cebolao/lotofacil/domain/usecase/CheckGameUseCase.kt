package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.AppError
import com.cebolao.lotofacil.domain.model.AppResult
import com.cebolao.lotofacil.domain.model.CheckReport
import com.cebolao.lotofacil.domain.model.DrawWindow
import com.cebolao.lotofacil.domain.model.FinancialCalculator
import com.cebolao.lotofacil.domain.model.Hit
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.GameCheckEngine
import com.cebolao.lotofacil.domain.service.TicketValidator
import com.cebolao.lotofacil.domain.service.ValidationResult
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.util.toAppError
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
            val validation = ticketValidator.validate(gameNumbers)
            if (validation is ValidationResult.Error) {
                emit(AppResult.Failure(AppError.Validation(validation.message)))
                return@flow
            }

            val syncResult = historyRepository.syncHistoryIfNeeded()
            if (syncResult.isFailure) {
                val error = syncResult.exceptionOrNull()?.toAppError() ?: AppError.Unknown(null)
                emit(AppResult.Failure(error))
                return@flow
            }

            val history = historyRepository.getHistory()
            if (history.isEmpty()) {
                emit(AppResult.Failure(AppError.Validation("Historico vazio")))
                return@flow
            }

            val game = LotofacilGame.fromNumbers(gameNumbers)
            val sourceHash = calculateTicketHash(game)

            val checkResult = gameCheckEngine.checkGame(game, history)

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
        } catch (e: Exception) {
            logger.error(TAG, "Check failed", e)
            emit(AppResult.Failure(e.toAppError()))
        }
    }.flowOn(defaultDispatcher)

    private fun calculateTicketHash(game: LotofacilGame): String {
        val numbers = game.numbers.sorted().joinToString(",")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(numbers.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
