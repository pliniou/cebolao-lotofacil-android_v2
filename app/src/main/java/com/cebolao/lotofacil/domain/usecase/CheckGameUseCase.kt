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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.security.MessageDigest
import javax.inject.Inject

class CheckGameUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val gameCheckEngine: GameCheckEngine,
    private val ticketValidator: TicketValidator,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(gameNumbers: Set<Int>): Flow<AppResult<CheckReport>> = flow {
        try {
            val validation = ticketValidator.validate(gameNumbers)
            if (validation is ValidationResult.Error) {
                emit(AppResult.Failure(AppError.Validation(validation.message)))
                return@flow
            }

            when (val syncResult = historyRepository.syncHistoryIfNeeded()) {
                is AppResult.Failure -> {
                    emit(syncResult)
                    return@flow
                }
                is AppResult.Success -> Unit
            }

            val history = when (val historyResult = historyRepository.getHistory()) {
                is AppResult.Failure -> {
                    emit(historyResult)
                    return@flow
                }
                is AppResult.Success -> historyResult.value
            }

            if (history.isEmpty()) {
                emit(AppResult.Failure(AppError.Validation("Empty history")))
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
            emit(AppResult.Failure(AppError.Unknown(e)))
        }
    }.flowOn(defaultDispatcher)

    private fun calculateTicketHash(game: LotofacilGame): String {
        val numbers = game.numbers.sorted().joinToString(",")
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(numbers.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
