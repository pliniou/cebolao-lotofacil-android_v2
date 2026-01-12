package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.GameScore
import com.cebolao.lotofacil.domain.model.GameStatisticsProvider
import com.cebolao.lotofacil.domain.model.MetricEvaluation
import com.cebolao.lotofacil.domain.model.ScoreStatus
import javax.inject.Inject

private const val POINTS_EXCELLENT = 10
private const val POINTS_GOOD = 7
private const val POINTS_WARNING = 3
private const val POINTS_BAD = 0
private const val SCORE_THRESHOLD_EXCELLENT = 90
private const val SCORE_THRESHOLD_GOOD = 70
private const val SCORE_THRESHOLD_WARNING = 50
private const val MAX_SCORE_PERCENT = 100

/**
 * Calculadora que centraliza e garante semântica consistente das métricas de jogo.
 * Encapsula tanto o cálculo bruto quanto a avaliação qualitativa (Score).
 */
class GameMetricsCalculator @Inject constructor() {
    fun calculate(provider: GameStatisticsProvider, lastDraw: Set<Int>? = null): GameComputedMetrics {
        val repeated = lastDraw?.let { provider.repeatedFrom(it) } ?: 0
        return GameComputedMetrics(
            sum = provider.sum,
            evens = provider.evens,
            primes = provider.primes,
            fibonacci = provider.fibonacci,
            frame = provider.frame,
            sequences = provider.sequences,
            multiplesOf3 = provider.multiplesOf3,
            center = provider.center,
            repeated = repeated
        )
    }

    fun analyze(provider: GameStatisticsProvider, lastDraw: Set<Int>? = null): GameScore {
        val numbers = provider.numbers
        if (numbers.size != GameConstants.GAME_SIZE) {
            return GameScore(0, ScoreStatus.BAD, emptyList())
        }

        val evals = mutableListOf<MetricEvaluation>().apply {
            add(evaluateSum(provider.sum))
            add(evaluateEvens(provider.evens))
            add(evaluatePrimes(provider.primes))
            add(evaluateFrame(provider.frame))
            add(evaluateFibonacci(provider.fibonacci))
            add(evaluateMultiplesOf3(provider.multiplesOf3))
            add(evaluateCenter(provider.center))
            add(evaluateSequences(provider.sequences))
            lastDraw?.let { add(evaluateRepeated(provider.repeatedFrom(it))) }
        }

        val points = evals.sumOf { getPointsForStatus(it.status) }
        val maxPoints = evals.size * POINTS_EXCELLENT
        val finalScore = if (maxPoints > 0) {
            (points.toFloat() / maxPoints * MAX_SCORE_PERCENT).toInt()
        } else {
            0
        }
        val totalStatus = when {
            finalScore >= SCORE_THRESHOLD_EXCELLENT -> ScoreStatus.EXCELLENT
            finalScore >= SCORE_THRESHOLD_GOOD -> ScoreStatus.GOOD
            finalScore >= SCORE_THRESHOLD_WARNING -> ScoreStatus.WARNING
            else -> ScoreStatus.BAD
        }
        return GameScore(finalScore, totalStatus, evals)
    }

    private fun getPointsForStatus(status: ScoreStatus): Int = when (status) {
        ScoreStatus.EXCELLENT -> POINTS_EXCELLENT
        ScoreStatus.GOOD -> POINTS_GOOD
        ScoreStatus.WARNING -> POINTS_WARNING
        ScoreStatus.BAD -> POINTS_BAD
    }

    private fun evaluateSum(sum: Int) = MetricEvaluation(
        "Soma", sum,
        when (sum) {
            in GameConstants.AnalysisRanges.SUM_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.AnalysisRanges.SUM_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        },
        when {
            sum < GameConstants.AnalysisRanges.SUM_ACCEPTABLE.first -> "Muito baixa"
            sum > GameConstants.AnalysisRanges.SUM_ACCEPTABLE.last -> "Muito alta"
            sum !in GameConstants.AnalysisRanges.SUM_IDEAL -> "Um pouco fora"
            else -> "Equilibrada"
        }
    )

    private fun evaluateEvens(evens: Int) = MetricEvaluation(
        "Pares", evens,
        when (evens) {
            in GameConstants.AnalysisRanges.EVENS_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.AnalysisRanges.EVENS_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        },
        when {
            evens < GameConstants.AnalysisRanges.EVENS_ACCEPTABLE.first -> "Muitos Ímpares"
            evens > GameConstants.AnalysisRanges.EVENS_ACCEPTABLE.last -> "Muitos Pares"
            else -> "Equilibrado"
        }
    )

    private fun evaluatePrimes(primes: Int) = MetricEvaluation(
        "Primos", primes,
        when (primes) {
            in GameConstants.AnalysisRanges.PRIMES_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.AnalysisRanges.PRIMES_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        },
        when {
            primes < GameConstants.AnalysisRanges.PRIMES_ACCEPTABLE.first -> "Poucos Primos"
            primes > GameConstants.AnalysisRanges.PRIMES_ACCEPTABLE.last -> "Muitos Primos"
            else -> "Ideal"
        }
    )

    private fun evaluateFrame(frame: Int) = MetricEvaluation(
        "Moldura", frame,
        when (frame) {
            in GameConstants.AnalysisRanges.FRAME_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.AnalysisRanges.FRAME_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        },
        when {
            frame < GameConstants.AnalysisRanges.FRAME_ACCEPTABLE.first -> "Centro carregado"
            frame > GameConstants.AnalysisRanges.FRAME_ACCEPTABLE.last -> "Borda carregada"
            else -> "Bem distribuído"
        }
    )

    private fun evaluateFibonacci(fib: Int) = evaluateStandard(
        "Fibonacci", fib, 
        GameConstants.AnalysisRanges.FIBONACCI_IDEAL, 
        GameConstants.AnalysisRanges.FIBONACCI_ACCEPTABLE
    )
    private fun evaluateMultiplesOf3(mult: Int) = evaluateStandard(
        "Múltiplos de 3", mult, 
        GameConstants.AnalysisRanges.MULTIPLES_OF_3_IDEAL, 
        GameConstants.AnalysisRanges.MULTIPLES_OF_3_ACCEPTABLE
    )
    private fun evaluateCenter(center: Int) = evaluateStandard(
        "Miolo", center, 
        GameConstants.AnalysisRanges.CENTER_IDEAL, 
        GameConstants.AnalysisRanges.CENTER_ACCEPTABLE
    )
    private fun evaluateSequences(seq: Int) = evaluateStandard(
        "Sequências (3+)", seq, 
        GameConstants.AnalysisRanges.SEQUENCES_IDEAL, 
        GameConstants.AnalysisRanges.SEQUENCES_ACCEPTABLE
    )
    private fun evaluateRepeated(repeated: Int) = evaluateStandard(
        "Repetidos", repeated, 
        GameConstants.AnalysisRanges.REPEATED_IDEAL, 
        GameConstants.AnalysisRanges.REPEATED_ACCEPTABLE
    )

    private fun evaluateStandard(name: String, value: Int, ideal: IntRange, acceptable: IntRange): MetricEvaluation {
        val status = when (value) {
            in ideal -> ScoreStatus.EXCELLENT
            in acceptable -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation(name, value, status, when (status) {
            ScoreStatus.EXCELLENT -> "Ideal"
            ScoreStatus.GOOD -> "Aceitável"
            else -> "Fora do padrão"
        })
    }
}
