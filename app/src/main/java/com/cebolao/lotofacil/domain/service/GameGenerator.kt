package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.FilterRule
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.toRule
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.service.solver.BacktrackingSolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameGenerator @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val metricsCalculator: GameMetricsCalculator,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
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

        try {
            val result = executeGenerationLoop(context, config)
            finalizeGeneration(result, context, quantity, seedVal)
        } catch (e: Exception) {
            // Re-throw cancellation
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(GenerationProgress.failed(GenerationFailureReason.GENERIC_ERROR))
        }
    }.flowOn(defaultDispatcher)

    private suspend fun initializeGenerationContext(
        quantity: Int,
        filters: List<FilterState>,
        rnd: Random,
        config: GeneratorConfig
    ): GenerationContext {
        val activeFilters = filters.filter { it.isEnabled }
        val historyRaw = when (val historyResult = historyRepository.getHistory()) {
            is com.cebolao.lotofacil.domain.model.AppResult.Success -> historyResult.value
            is com.cebolao.lotofacil.domain.model.AppResult.Failure -> emptyList()
        }
        val history = historyRaw.map { LotofacilGame.fromNumbers(it.numbers) }.take(GameConstants.HISTORY_CHECK_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers ?: emptySet()
        
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
            metricsCalculator = metricsCalculator,
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
    
    private suspend fun tryRandomGeneration(context: GenerationContext, config: GeneratorConfig): Boolean {
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
            
            // Periodically yield to prevent blocking the UI thread too long
            if (attempts % 50 == 0) {
                yield()
            }
        }
        
        return false
    }
    
    private fun trackRejection(context: GenerationContext, metrics: GameComputedMetrics) {
        val failedRule = context.rules.firstOrNull { !it.matches(metrics) }
        if (failedRule != null) {
            context.rejections[failedRule.type] = (context.rejections[failedRule.type] ?: 0) + 1
            // Removed verbose debug logging in tight loop to prevent log flooding and overhead
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
    
    private suspend fun tryBacktrackingGeneration(
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
        quantity: Int,
        seedVal: Long
    ) {
        if (result.games.isNotEmpty()) {
            val telemetry = createTelemetry(result, context, seedVal)
            emit(GenerationProgress.finished(result.games, telemetry))
        } else {
            handleGenerationFailure(context)
        }
    }
    
    private fun createTelemetry(
        result: GenerationResult,
        context: GenerationContext,
        seedVal: Long
    ): GenerationTelemetry {
        return GenerationTelemetry(
            seed = seedVal,
            strategy = result.strategy,
            durationMs = result.duration,
            totalAttempts = result.totalAttempts,
            successfulGames = result.games.size,
            rejectionsByFilter = result.rejections
        )
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
}
