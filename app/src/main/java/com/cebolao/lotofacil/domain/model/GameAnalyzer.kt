package com.cebolao.lotofacil.domain.model

import com.cebolao.lotofacil.domain.GameConstants

enum class ScoreStatus {
    EXCELLENT, GOOD, WARNING, BAD
}

data class MetricEvaluation(
    val name: String,
    val value: Int,
    val status: ScoreStatus,
    val message: String
)

data class GameScore(
    val totalScore: Int, // 0-100
    val status: ScoreStatus,
    val evaluations: List<MetricEvaluation>
)

object GameAnalyzer {

    // Ranges moved to GameConstants

    fun analyze(numbers: Set<Int>, lastDraw: Set<Int>? = null): GameScore {
        if (numbers.size != 15) {
             return GameScore(0, ScoreStatus.BAD, emptyList())
        }
        
        val stats = object : GameStatisticsProvider {
            override val numbers = numbers
        }
        
        val evals = mutableListOf(
            evaluateSum(stats.sum),
            evaluateEvens(stats.evens),
            evaluatePrimes(stats.primes),
            evaluateFrame(stats.frame),
            evaluateFibonacci(stats.fibonacci),
            evaluateMultiplesOf3(stats.multiplesOf3),
            evaluateCenter(stats.center),
            evaluateSequences(stats.sequences)
        )

        // Only evaluate repeated if we have the last draw data
        if (lastDraw != null) {
            evals.add(evaluateRepeated(stats.repeatedFrom(lastDraw)))
        }
        
        val points = evals.sumOf { getPointsForStatus(it.status) }
        val maxPoints = evals.size * 10 
        
        // Normalize score to 100
        val finalScore = if (maxPoints > 0) (points.toFloat() / maxPoints * 100).toInt() else 0
        
        val totalStatus = when {
            finalScore >= 90 -> ScoreStatus.EXCELLENT
            finalScore >= 70 -> ScoreStatus.GOOD
            finalScore >= 50 -> ScoreStatus.WARNING
            else -> ScoreStatus.BAD
        }

        return GameScore(finalScore, totalStatus, evals)
    }

    private fun getPointsForStatus(status: ScoreStatus): Int = when(status) {
        ScoreStatus.EXCELLENT -> 10
        ScoreStatus.GOOD -> 7
        ScoreStatus.WARNING -> 3
        ScoreStatus.BAD -> 0
    }

    private fun evaluateSum(sum: Int): MetricEvaluation {
        val status = when (sum) {
            in GameConstants.SUM_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.SUM_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Soma", sum, status, getSumMessage(sum))
    }

    private fun evaluateEvens(evens: Int): MetricEvaluation {
        val status = when (evens) {
            in GameConstants.EVENS_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.EVENS_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Pares", evens, status, getEvensMessage(evens))
    }

    private fun evaluatePrimes(primes: Int): MetricEvaluation {
        val status = when (primes) {
            in GameConstants.PRIMES_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.PRIMES_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Primos", primes, status, getPrimesMessage(primes))
    }

    private fun evaluateFrame(frame: Int): MetricEvaluation {
        val status = when (frame) {
            in GameConstants.FRAME_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.FRAME_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Moldura", frame, status, getFrameMessage(frame))
    }

    private fun evaluateFibonacci(fib: Int): MetricEvaluation {
        val status = when (fib) {
            in GameConstants.FIBONACCI_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.FIBONACCI_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Fibonacci", fib, status, getSimpleMessage(status))
    }

    private fun evaluateMultiplesOf3(mult: Int): MetricEvaluation {
        val status = when (mult) {
            in GameConstants.MULTIPLES_OF_3_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.MULTIPLES_OF_3_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Múltiplos de 3", mult, status, getSimpleMessage(status))
    }

    private fun evaluateCenter(center: Int): MetricEvaluation {
        val status = when (center) {
            in GameConstants.CENTER_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.CENTER_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Miolo", center, status, getSimpleMessage(status))
    }

    private fun evaluateSequences(seq: Int): MetricEvaluation {
        val status = when (seq) {
            in GameConstants.SEQUENCES_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.SEQUENCES_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Sequências (3+)", seq, status, getSimpleMessage(status))
    }

    private fun evaluateRepeated(repeated: Int): MetricEvaluation {
        val status = when (repeated) {
            in GameConstants.REPEATED_IDEAL -> ScoreStatus.EXCELLENT
            in GameConstants.REPEATED_ACCEPTABLE -> ScoreStatus.GOOD
            else -> ScoreStatus.WARNING
        }
        return MetricEvaluation("Repetidos", repeated, status, getSimpleMessage(status))
    }

    private fun getSumMessage(sum: Int): String = when {
        sum < GameConstants.SUM_ACCEPTABLE.first -> "Muito baixa"
        sum > GameConstants.SUM_ACCEPTABLE.last -> "Muito alta"
        sum !in GameConstants.SUM_IDEAL -> "Um pouco fora"
        else -> "Equilibrada"
    }

    private fun getEvensMessage(evens: Int): String = when {
         evens < GameConstants.EVENS_ACCEPTABLE.first -> "Muitos Ímpares"
         evens > GameConstants.EVENS_ACCEPTABLE.last -> "Muitos Pares"
         evens !in GameConstants.EVENS_IDEAL -> "Aceitável"
         else -> "Equilibrado"
    }

    private fun getPrimesMessage(primes: Int): String = when {
        primes < GameConstants.PRIMES_ACCEPTABLE.first -> "Poucos Primos"
        primes > GameConstants.PRIMES_ACCEPTABLE.last -> "Muitos Primos"
        else -> "Ideal"
    }
    
    private fun getFrameMessage(frame: Int): String = when {
        frame < GameConstants.FRAME_ACCEPTABLE.first -> "Centro carregado"
        frame > GameConstants.FRAME_ACCEPTABLE.last -> "Borda carregada"
        else -> "Bem distribuído"
    }

    private fun getSimpleMessage(status: ScoreStatus): String = when(status) {
        ScoreStatus.EXCELLENT -> "Ideal"
        ScoreStatus.GOOD -> "Aceitável"
        ScoreStatus.WARNING -> "Fora do padrão"
        ScoreStatus.BAD -> "Ruim"
    }
}
