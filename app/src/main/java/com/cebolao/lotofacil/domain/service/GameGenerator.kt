package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.FilterRule
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.toRule
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.util.Logger
import com.cebolao.lotofacil.domain.util.NoOpLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val TAG = "GameGenerator"

class GameGenerator @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val logger: Logger = NoOpLogger()
) {
    /**
     * Generates lottery games based on configured filters.
     * 
     * @param quantity Number of games to generate (1-30)
     * @param filters Active filter constraints
     * @param config Generation configuration (timeout, attempts, etc.)
     * @param seed Random seed for reproducibility (optional)
     * @return Flow emitting generation progress and final results
     */
    fun generate(
        quantity: Int,
        filters: List<FilterState>,
        config: GeneratorConfig = GeneratorConfig.BALANCED,
        seed: Long? = null
    ): Flow<GenerationProgress> = flow {
        val seedVal = seed ?: System.currentTimeMillis()
        val rnd = Random(seedVal)
        emit(GenerationProgress.started(quantity))

        if (quantity <= 0) {
            emit(GenerationProgress.finished(emptyList()))
            return@flow
        }

        val context = initializeGenerationContext(quantity, filters, rnd, config)
        emit(GenerationProgress.step(GenerationStep.HEURISTIC_START, 0, quantity))

        val result = executeGenerationLoop(context, config)
        finalizeGeneration(result, context, quantity)
    }.flowOn(Dispatchers.Default)

    private suspend fun initializeGenerationContext(
        quantity: Int,
        filters: List<FilterState>,
        rnd: Random,
        config: GeneratorConfig
    ): GenerationContext {
        val activeFilters = filters.filter { it.isEnabled }
        val historyRaw = historyRepository.getHistory()
        val history = historyRaw.map { LotofacilGame.fromNumbers(it.numbers) }.take(GameConstants.HISTORY_CHECK_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers ?: emptySet()
        
        logger.info(TAG, "Starting generation: quantity=$quantity, activeFilters=${activeFilters.size}, config=$config")
        
        return GenerationContext(
            quantity = quantity,
            activeFilters = activeFilters,
            generatedGames = LinkedHashSet(quantity),
            lastDrawNumbers = lastDrawNumbers,
            requiresLastDraw = activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR },
            strategyUsed = if (activeFilters.isEmpty()) GenerationStep.RANDOM_START else GenerationStep.HEURISTIC_START,
            startTime = System.currentTimeMillis(),
            rejections = mutableMapOf(),
            totalAttempts = 0,
            rules = activeFilters.mapNotNull { it.toRule() },
            metricsCalculator = GameMetricsCalculator(),
            rnd = rnd,
            history = history
        )
    }
    
    private suspend fun FlowCollector<GenerationProgress>.executeGenerationLoop(
        context: GenerationContext,
        config: GeneratorConfig
    ): GenerationResult {
        var solver: BacktrackingSolver? = null
        var lastProgressAt = System.currentTimeMillis()
        
        while (context.generatedGames.size < context.quantity && currentCoroutineContext().isActive) {
            if (isTimeoutReached(lastProgressAt, config)) {
                logger.warning(TAG, "Generation timeout reached. Generated ${context.generatedGames.size}/${context.quantity} games")
                context.strategyUsed = GenerationStep.RANDOM_FALLBACK
                break
            }
            
            if (tryRandomGeneration(context, config)) {
                lastProgressAt = System.currentTimeMillis()
                emit(GenerationProgress.attempt(context.generatedGames.size, context.quantity))
                continue
            }
            
            solver = initializeBacktrackingSolver(solver, context, config)
            
            if (!tryBacktrackingGeneration(solver, context, config)) {
                context.strategyUsed = GenerationStep.RANDOM_FALLBACK
                break
            }
            
            lastProgressAt = System.currentTimeMillis()
            emit(GenerationProgress.attempt(context.generatedGames.size, context.quantity))
        }
        
        return GenerationResult(
            games = context.generatedGames.toList(),
            duration = System.currentTimeMillis() - context.startTime,
            totalAttempts = context.totalAttempts,
            rejections = context.rejections,
            strategy = context.strategyUsed
        )
    }
    
    private fun isTimeoutReached(lastProgressAt: Long, config: GeneratorConfig): Boolean {
        val now = System.currentTimeMillis()
        return now - lastProgressAt > config.timeoutMs
    }
    
    private fun tryRandomGeneration(context: GenerationContext, config: GeneratorConfig): Boolean {
        var attempts = 0
        
        while (attempts < config.maxRandomAttempts) {
            context.totalAttempts++
            val candidate = generateRandomGame(context.rnd)
            
            val metrics = context.metricsCalculator.calculate(candidate, context.lastDrawNumbers)
            if (context.rules.all { it.matches(metrics) }) {
                if (isDiverseEnough(candidate, context.generatedGames, config.diversityThreshold)) {
                    if (context.generatedGames.add(candidate)) {
                        return true
                    }
                }
            } else {
                trackRejection(context, metrics)
            }
            attempts++
        }
        
        return false
    }
    
    private fun trackRejection(context: GenerationContext, metrics: GameComputedMetrics) {
        val failedRule = context.rules.firstOrNull { !it.matches(metrics) }
        if (failedRule != null) {
            context.rejections[failedRule.type] = (context.rejections[failedRule.type] ?: 0) + 1
            logger.debug(TAG, "Game rejected by filter: ${failedRule.type}")
        }
    }
    
    private fun initializeBacktrackingSolver(
        existingSolver: BacktrackingSolver?,
        context: GenerationContext,
        config: GeneratorConfig
    ): BacktrackingSolver? {
        if (!config.enableBacktracking) return existingSolver
        
        return existingSolver ?: BacktrackingSolver(
            context.activeFilters,
            context.lastDrawNumbers,
            context.rnd,
            onAttempt = { context.totalAttempts++ },
            onRejection = { type -> context.rejections[type] = (context.rejections[type] ?: 0) + 1 }
        )
    }
    
    private fun tryBacktrackingGeneration(
        solver: BacktrackingSolver?,
        context: GenerationContext,
        config: GeneratorConfig
    ): Boolean {
        val backtrackGame = solver?.findValidGame()
        if (backtrackGame != null) {
            if (isDiverseEnough(backtrackGame, context.generatedGames, config.diversityThreshold)) {
                if (context.generatedGames.add(backtrackGame)) {
                    return true
                }
            }
        }
        return false
    }
    
    private suspend fun FlowCollector<GenerationProgress>.finalizeGeneration(
        result: GenerationResult,
        context: GenerationContext,
        quantity: Int
    ) {
        logger.info(TAG, "Generation completed: ${result.games.size}/$quantity games in ${result.duration}ms, attempts=${result.totalAttempts}")
        
        if (result.games.isNotEmpty()) {
            val telemetry = createTelemetry(result, context)
            logTelemetryDetails(telemetry, context.rejections)
            emit(GenerationProgress.finished(result.games, telemetry))
        } else {
            handleGenerationFailure(context)
        }
    }
    
    private fun createTelemetry(result: GenerationResult, context: GenerationContext): GenerationTelemetry {
        return GenerationTelemetry(
            seed = context.rnd.nextInt().toLong(),
            strategy = result.strategy,
            durationMs = result.duration,
            totalAttempts = result.totalAttempts,
            successfulGames = result.games.size,
            rejectionsByFilter = result.rejections
        )
    }
    
    private fun logTelemetryDetails(telemetry: GenerationTelemetry, rejections: Map<FilterType, Int>) {
        logger.info(TAG, "Telemetry - Success rate: ${"%.1f".format(telemetry.successRate * 100)}%, Avg time/game: ${telemetry.avgTimePerGame}ms")
        telemetry.mostRestrictiveFilter?.let { filter ->
            logger.info(TAG, "Most restrictive filter: $filter (${rejections[filter]} rejections)")
        }
    }
    
    private suspend fun FlowCollector<GenerationProgress>.handleGenerationFailure(context: GenerationContext) {
        if (context.history.isEmpty() && context.requiresLastDraw) {
            emit(GenerationProgress.failed(GenerationFailureReason.NO_HISTORY))
            return
        }
        
        val reason = if (context.activeFilters.isNotEmpty()) {
            GenerationFailureReason.FILTERS_TOO_STRICT
        } else {
            GenerationFailureReason.GENERIC_ERROR
        }
        logger.error(TAG, "Failed to generate games: $reason")
        emit(GenerationProgress.failed(reason))
    }
    
    private data class GenerationContext(
        val quantity: Int,
        val activeFilters: List<FilterState>,
        val generatedGames: MutableSet<LotofacilGame>,
        val lastDrawNumbers: Set<Int>,
        val requiresLastDraw: Boolean,
        var strategyUsed: GenerationStep,
        val startTime: Long,
        val rejections: MutableMap<FilterType, Int>,
        var totalAttempts: Int,
        val rules: List<FilterRule>,
        val metricsCalculator: GameMetricsCalculator,
        val rnd: Random,
        val history: List<LotofacilGame>
    )
    
    private data class GenerationResult(
        val games: List<LotofacilGame>,
        val duration: Long,
        val totalAttempts: Int,
        val rejections: Map<FilterType, Int>,
        val strategy: GenerationStep
    )
    
    private fun generateRandomGame(random: Random): LotofacilGame {
         // Simple shuffle
         val numbers = GameConstants.ALL_NUMBERS
             .shuffled(random)
             .take(GameConstants.GAME_SIZE)
             .toSet()
         return LotofacilGame.fromNumbers(numbers)
    }
    
    private fun isDiverseEnough(
        candidate: LotofacilGame,
        existing: Set<LotofacilGame>,
        threshold: Int
    ): Boolean {
        if (existing.isEmpty()) return true
        return existing.none { 
            val intersection = candidate.numbers.intersect(it.numbers).size
            intersection >= threshold 
        }
    }

    private class BacktrackingSolver(
        filters: List<FilterState>,
        private val lastDrawNumbers: Set<Int>,
        private val rnd: Random,
        private val onAttempt: () -> Unit,
        private val onRejection: (FilterType) -> Unit
    ) {

        private val sumBounds: IntRange? = filters.boundFor(FilterType.SOMA_DEZENAS)
        private val evensBounds: IntRange? = filters.boundFor(FilterType.PARES)
        private val primesBounds: IntRange? = filters.boundFor(FilterType.PRIMOS)
        private val fibonacciBounds: IntRange? = filters.boundFor(FilterType.FIBONACCI)
        private val frameBounds: IntRange? = filters.boundFor(FilterType.MOLDURA)
        private val repeatedBounds: IntRange? = filters.boundFor(FilterType.REPETIDAS_CONCURSO_ANTERIOR)
        private val rules: List<FilterRule> = filters.mapNotNull { it.toRule() }
        private val currentSelection = IntArray(GameConstants.GAME_SIZE)
        private val candidates = GameConstants.ALL_NUMBERS.toIntArray()
        private var lastSolution: LotofacilGame? = null
        private val maxN = GameConstants.MAX_NUMBER
        private val evensAfter = IntArray(maxN + 1)
        private val primesAfter = IntArray(maxN + 1)
        private val fibAfter = IntArray(maxN + 1)
        private val frameAfter = IntArray(maxN + 1)
        private val repeatedAfter = IntArray(maxN + 1)

        init {
            for (n in maxN downTo 0) {
                val next = n + 1
                if (next > maxN) {
                    evensAfter[n] = 0
                    primesAfter[n] = 0
                    fibAfter[n] = 0
                    frameAfter[n] = 0
                    repeatedAfter[n] = 0
                } else {
                    evensAfter[n] = evensAfter[next] + if (next % 2 == 0) 1 else 0
                    primesAfter[n] = primesAfter[next] + if (GameConstants.PRIMOS.contains(next)) 1 else 0
                    fibAfter[n] = fibAfter[next] + if (GameConstants.FIBONACCI.contains(next)) 1 else 0
                    frameAfter[n] = frameAfter[next] + if (GameConstants.MOLDURA.contains(next)) 1 else 0
                    repeatedAfter[n] = repeatedAfter[next] + if (lastDrawNumbers.contains(next)) 1 else 0
                }
            }
        }

        fun findValidGame(): LotofacilGame? {
            lastSolution = null
            shuffleInPlace(candidates, rnd)
            for (i in currentSelection.indices) currentSelection[i] = 0

            val ok = solve(
                index = 0,
                currentSum = 0,
                currentEvens = 0,
                currentPrimes = 0,
                currentFib = 0,
                currentFrame = 0,
                currentRepeated = 0,
                lastNum = 0
            )
            return if (ok) lastSolution else null
        }

        private fun solve(
            index: Int,
            currentSum: Int,
            currentEvens: Int,
            currentPrimes: Int,
            currentFib: Int,
            currentFrame: Int,
            currentRepeated: Int,
            lastNum: Int
        ): Boolean {
            if (index == GameConstants.GAME_SIZE) {
                return validateCompleteGame(
                    sum = currentSum,
                    evens = currentEvens,
                    primes = currentPrimes,
                    fib = currentFib,
                    frame = currentFrame,
                    repeated = currentRepeated
                )
            }

            if (!isBasicFeasible(index, lastNum)) return false
            if (!areBoundsFeasible(currentSum, currentEvens, currentPrimes, currentFib, currentFrame, currentRepeated, index, lastNum)) return false

            return tryNumberSelection(index, currentSum, currentEvens, currentPrimes, currentFib, currentFrame, currentRepeated, lastNum)
        }

        private fun isBasicFeasible(index: Int, lastNum: Int): Boolean {
            val remainingCount = GameConstants.GAME_SIZE - index
            val availableCount = maxN - lastNum
            return availableCount >= remainingCount
        }

        private fun areBoundsFeasible(
            currentSum: Int,
            currentEvens: Int,
            currentPrimes: Int,
            currentFib: Int,
            currentFrame: Int,
            currentRepeated: Int,
            index: Int,
            lastNum: Int
        ): Boolean {
            val remainingCount = GameConstants.GAME_SIZE - index
            val availableCount = maxN - lastNum

            if (!isSumFeasible(currentSum, lastNum, remainingCount)) return false
            if (!isCountFeasibleForType(currentEvens, evensBounds, remainingCount, evensAfter[lastNum], availableCount - evensAfter[lastNum])) return false
            if (!isCountFeasibleForType(currentPrimes, primesBounds, remainingCount, primesAfter[lastNum], availableCount - primesAfter[lastNum])) return false
            if (!isCountFeasibleForType(currentFib, fibonacciBounds, remainingCount, fibAfter[lastNum], availableCount - fibAfter[lastNum])) return false
            if (!isCountFeasibleForType(currentFrame, frameBounds, remainingCount, frameAfter[lastNum], availableCount - frameAfter[lastNum])) return false
            if (!isCountFeasibleForType(currentRepeated, repeatedBounds, remainingCount, repeatedAfter[lastNum], availableCount - repeatedAfter[lastNum])) return false

            return true
        }

        private fun isSumFeasible(currentSum: Int, lastNum: Int, remainingCount: Int): Boolean {
            if (sumBounds == null) return true

            val minPossibleSum = currentSum + minSumAfter(lastNum, remainingCount)
            val maxPossibleSum = currentSum + maxSumAfter(lastNum, remainingCount)
            return minPossibleSum <= sumBounds.last && maxPossibleSum >= sumBounds.first
        }

        private fun isCountFeasibleForType(
            current: Int,
            bounds: IntRange?,
            remainingCount: Int,
            availableMatch: Int,
            availableNonMatch: Int
        ): Boolean {
            if (bounds == null) return true
            return isCountFeasible(current, bounds, remainingCount, availableMatch, availableNonMatch)
        }

        private fun tryNumberSelection(
            index: Int,
            currentSum: Int,
            currentEvens: Int,
            currentPrimes: Int,
            currentFib: Int,
            currentFrame: Int,
            currentRepeated: Int,
            lastNum: Int
        ): Boolean {
            for (i in candidates.indices) {
                val num = candidates[i]
                if (num <= lastNum) continue

                currentSelection[index] = num

                if (solve(
                        index = index + 1,
                        currentSum = currentSum + num,
                        currentEvens = currentEvens + if (num % 2 == 0) 1 else 0,
                        currentPrimes = currentPrimes + if (GameConstants.PRIMOS.contains(num)) 1 else 0,
                        currentFib = currentFib + if (GameConstants.FIBONACCI.contains(num)) 1 else 0,
                        currentFrame = currentFrame + if (GameConstants.MOLDURA.contains(num)) 1 else 0,
                        currentRepeated = currentRepeated + if (lastDrawNumbers.contains(num)) 1 else 0,
                        lastNum = num
                    )
                ) {
                    return true
                }
                currentSelection[index] = 0
            }

            return false
        }

        private fun validateCompleteGame(
            sum: Int, evens: Int, primes: Int, fib: Int, frame: Int, repeated: Int
        ): Boolean {
            onAttempt()

            val game = LotofacilGame.fromNumbers(currentSelection.toSet())

            val metrics = GameComputedMetrics(
                sum = sum,
                evens = evens,
                primes = primes,
                fibonacci = fib,
                frame = frame,
                repeated = repeated,
                sequences = game.sequences,
                multiplesOf3 = game.multiplesOf3,
                center = game.center
            )
            
            val failedRule = rules.firstOrNull { !it.matches(metrics) }
            if (failedRule != null) {
                onRejection(failedRule.type)
                return false
            }

            lastSolution = game
            return true
        }

        private fun minSumAfter(lastNum: Int, count: Int): Int {
            val start = lastNum + 1
            val end = start + count - 1
            if (start > maxN || end > maxN) return Int.MAX_VALUE / 4
            return (count * (start + end)) / 2
        }

        private fun maxSumAfter(lastNum: Int, count: Int): Int {
            if (maxN - lastNum < count) return Int.MIN_VALUE / 4
            val start = maxN - count + 1
            val end = maxN
            return (count * (start + end)) / 2
        }

        private fun isCountFeasible(
            current: Int,
            bounds: IntRange,
            remainingCount: Int,
            availableMatch: Int,
            availableNonMatch: Int
        ): Boolean {
            if (current > bounds.last) return false

            val maxAdditional = min(remainingCount, availableMatch)
            val minAdditional = max(0, remainingCount - availableNonMatch)
            val minTotal = current + minAdditional
            val maxTotal = current + maxAdditional

            return maxTotal >= bounds.first && minTotal <= bounds.last
        }

        private fun List<FilterState>.boundFor(type: FilterType): IntRange? {
            val range = firstOrNull { it.type == type }?.selectedRange ?: return null
            return range.start.toInt()..range.endInclusive.toInt()
        }

        private fun shuffleInPlace(array: IntArray, random: Random) {
            for (i in array.lastIndex downTo 1) {
                val j = random.nextInt(i + 1)
                val tmp = array[i]
                array[i] = array[j]
                array[j] = tmp
            }
        }
    }
}
