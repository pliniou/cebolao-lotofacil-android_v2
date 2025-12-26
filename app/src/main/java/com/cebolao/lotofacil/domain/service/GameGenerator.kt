package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.FilterRule
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.toRule
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val BATCH_SIZE = 50
private const val TIMEOUT_MS = 10000L
private const val MAX_RANDOM_ATTEMPTS = 500
private const val DIVERSITY_THRESHOLD = 14

class GameGenerator @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    fun generate(quantity: Int, filters: List<FilterState>, seed: Long? = null): Flow<GenerationProgress> = flow {
        val seedVal = seed ?: System.currentTimeMillis()
        val rnd = Random(seedVal)
        emit(GenerationProgress.started(quantity))

        if (quantity <= 0) {
            emit(GenerationProgress.finished(emptyList()))
            return@flow
        }

        val activeFilters = filters.filter { it.isEnabled }
        val generatedGames = LinkedHashSet<LotofacilGame>(quantity)

        val history = historyRepository.getHistory().take(GameConstants.HISTORY_CHECK_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers ?: emptySet()
        val requiresLastDraw = activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }

        var strategyUsed = if (activeFilters.isEmpty()) GenerationStep.RANDOM_START else GenerationStep.HEURISTIC_START
        val startTime = System.currentTimeMillis()
        val rejections = mutableMapOf<FilterType, Int>()
        var totalAttempts = 0

        // Determine rules for random validation
        val rules = activeFilters.mapNotNull { it.toRule(lastDrawNumbers) }
        val metricsCalculator = GameMetricsCalculator()

        // Fallback solver (lazy init)
        var solver: BacktrackingSolver? = null
        
        emit(GenerationProgress.step(GenerationStep.HEURISTIC_START, 0, quantity))

        var lastProgressAt = System.currentTimeMillis()

        while (generatedGames.size < quantity && currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            if (now - lastProgressAt > TIMEOUT_MS) {
                strategyUsed = GenerationStep.RANDOM_FALLBACK
                emit(GenerationProgress.step(GenerationStep.RANDOM_FALLBACK, generatedGames.size, quantity))
                break 
            }

            // Try Random Generation First
            var success = false
            var attempts = 0
            
            while (attempts < MAX_RANDOM_ATTEMPTS) {
                totalAttempts++
                val candidate = generateRandomGame(rnd)
                
                // 1. Validator Compliance
                val metrics = metricsCalculator.calculate(candidate, lastDrawNumbers)
                if (rules.all { it.matches(metrics) }) {
                     // 2. Diversity Check
                    if (isDiverseEnough(candidate, generatedGames)) {
                        if (generatedGames.add(candidate)) {
                            success = true
                            break
                        }
                    }
                } else {
                     // Track rejection (first failed rule)
                     val failedRule = rules.firstOrNull { !it.matches(metrics) }
                     if (failedRule != null) {
                        rejections[failedRule.type] = (rejections[failedRule.type] ?: 0) + 1
                     }
                }
                attempts++
            }

            if (success) {
                lastProgressAt = System.currentTimeMillis()
                emit(GenerationProgress.attempt(generatedGames.size, quantity))
                continue
            }

            // Fallback to Backtracking if Random fails efficiently
            if (solver == null) {
                solver = BacktrackingSolver(activeFilters, lastDrawNumbers, rnd,
                    onAttempt = { totalAttempts++ },
                    onRejection = { type -> rejections[type] = (rejections[type] ?: 0) + 1 }
                )
            }
            
            val backtrackGame = solver!!.findValidGame()
            if (backtrackGame != null) {
                 if (isDiverseEnough(backtrackGame, generatedGames)) {
                     if (generatedGames.add(backtrackGame)) {
                        lastProgressAt = System.currentTimeMillis()
                        emit(GenerationProgress.attempt(generatedGames.size, quantity))
                     }
                 }
            } else {
                // Solver exhausted or stuck
               break
            }
        }
        
        // Fill remaining with random if needed/requested behavior (or fail?)
        // Original logic filled with random if filters were empty, 
        // but if filters exist and we timed out, we should probably return what we have or try random fallback (which we broke out to)
        if (generatedGames.size < quantity && currentCoroutineContext().isActive) {
             // Continue with pure random if fallback enabled
             while (generatedGames.size < quantity) {
                 generatedGames.add(generateRandomGame(rnd))
             }
        }

        val duration = System.currentTimeMillis() - startTime

        if (generatedGames.isNotEmpty()) {
            val telemetry = GenerationTelemetry(
                seed = seedVal,
                strategy = strategyUsed,
                durationMs = duration,
                totalAttempts = totalAttempts,
                rejectionsByFilter = rejections
            )
            emit(GenerationProgress.finished(generatedGames.toList(), telemetry))
        } else {
            if (activeFilters.isNotEmpty()) {
                 val reason = if (requiresLastDraw && history.isEmpty()) {
                    GenerationFailureReason.NO_HISTORY
                } else {
                    GenerationFailureReason.FILTERS_TOO_STRICT
                }
                emit(GenerationProgress.failed(reason))
            } else {
                emit(GenerationProgress.failed(GenerationFailureReason.GENERIC_ERROR))
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun generateRandomGame(random: Random): LotofacilGame {
         // Simple shuffle
         val numbers = GameConstants.ALL_NUMBERS
             .shuffled(random)
             .take(GameConstants.GAME_SIZE)
             .toSet()
         return LotofacilGame.fromNumbers(numbers)
    }
    
    private fun isDiverseEnough(candidate: LotofacilGame, existing: Set<LotofacilGame>): Boolean {
        if (existing.isEmpty()) return true
        return existing.none { 
            val intersection = candidate.numbers.intersect(it.numbers).size
            intersection >= DIVERSITY_THRESHOLD 
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

        private val rules: List<FilterRule> = filters.mapNotNull { it.toRule(lastDrawNumbers) }

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

            val remainingCount = GameConstants.GAME_SIZE - index
            val availableCount = maxN - lastNum
            if (availableCount < remainingCount) return false

            if (sumBounds != null) {
                val minPossibleSum = currentSum + minSumAfter(lastNum, remainingCount)
                val maxPossibleSum = currentSum + maxSumAfter(lastNum, remainingCount)
                if (minPossibleSum > sumBounds.last || maxPossibleSum < sumBounds.first) return false
            }

            if (evensBounds != null && !isCountFeasible(currentEvens, evensBounds, remainingCount, evensAfter[lastNum], availableCount - evensAfter[lastNum])) return false
            if (primesBounds != null && !isCountFeasible(currentPrimes, primesBounds, remainingCount, primesAfter[lastNum], availableCount - primesAfter[lastNum])) return false
            if (fibonacciBounds != null && !isCountFeasible(currentFib, fibonacciBounds, remainingCount, fibAfter[lastNum], availableCount - fibAfter[lastNum])) return false
            if (frameBounds != null && !isCountFeasible(currentFrame, frameBounds, remainingCount, frameAfter[lastNum], availableCount - frameAfter[lastNum])) return false
            if (repeatedBounds != null && !isCountFeasible(currentRepeated, repeatedBounds, remainingCount, repeatedAfter[lastNum], availableCount - repeatedAfter[lastNum])) return false

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
            
            // Construct metrics directly from state + lazy structure calculation
            val structural = calculateStructuralMetrics(currentSelection)
            
            val metrics = GameComputedMetrics(
                sum = sum,
                evens = evens,
                primes = primes,
                fibonacci = fib,
                frame = frame,
                repeated = repeated,
                sequences = structural.sequences,
                lines = structural.lines,
                columns = structural.columns,
                quadrants = structural.quadrants
            )
            
            val failedRule = rules.firstOrNull { !it.matches(metrics) }
            if (failedRule != null) {
                onRejection(failedRule.type)
                return false
            }

            // Success! Create the game object.
            // Note: currentSelection is sorted. LotofacilGame expects a set (which usually implies sorted numbers for the mask).
            // We don't need to shuffle here because 'candidates' was shuffled before starting backtrack.
            
            val numbers = LinkedHashSet<Int>(GameConstants.GAME_SIZE)
            for (n in currentSelection) numbers.add(n)
            
            lastSolution = LotofacilGame.fromNumbers(numbers)
            return true
        }

        private data class StructuralMetrics(val lines: Int, val columns: Int, val quadrants: Int, val sequences: Int)

        private fun calculateStructuralMetrics(selection: IntArray): StructuralMetrics {
             var sequences = 0
             var currentSeq = 0
             
             // Arrays for grid counts
             val linesArr = IntArray(5)
             val colsArr = IntArray(5)
             val quadsArr = BooleanArray(4)

             var prev = -1
             
             for (n in selection) {
                 // Sequence
                 if (prev != -1 && n == prev + 1) {
                     currentSeq++
                 } else {
                     if (currentSeq >= 3) sequences++ // 3+ numbers sequence? Rule usually says "Sequencia de X numeros". 
                     // Wait, standard definition is "sequence of numbers", usually handled by 'sequences' metric counting sequences of size >= 3? or just total numbers in sequence? 
                     // StatisticAnalyzer says: "if (run >= 3) count++". Yes.
                     currentSeq = 1 // reset to 1 (the current number)
                 }
                 prev = n

                 // Grid
                 // Board is 1..25. Row = (n-1)/5. Col = (n-1)%5.
                 val idx = n - 1
                 val r = idx / 5
                 val c = idx % 5
                 linesArr[r]++
                 colsArr[c]++
                 
                 // Quadrants: 0 (TL), 1 (TR), 2 (BL), 3 (BR)
                 // TL: r<2, c<2. TR: r<2, c>=2 (col 2,3,4? Wait 5 cols. 0,1 | 2,3,4? Or split at 2?)
                 // StatisticAnalyzer: qr = if (row < 2) 0 else 1. qc = if (col < 2) 0 else 1. q = qr*2 + qc.
                 // So rows 0,1 -> Top. Rows 2,3,4 -> Bottom. Cols 0,1 -> Left. Cols 2,3,4 -> Right.
                 // This matches 2x2 split roughly.
                 
                 val qr = if (r < 2) 0 else 1
                 val qc = if (c < 2) 0 else 1
                 quadsArr[qr * 2 + qc] = true
             }
             if (currentSeq >= 3) sequences++

             var lCount = 0
             for (c in linesArr) if (c >= 3) lCount++
             
             var cCount = 0
             for (c in colsArr) if (c >= 3) cCount++
             
             var qCount = 0
             for (b in quadsArr) if (b) qCount++
             
             return StructuralMetrics(lCount, cCount, qCount, sequences)
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
