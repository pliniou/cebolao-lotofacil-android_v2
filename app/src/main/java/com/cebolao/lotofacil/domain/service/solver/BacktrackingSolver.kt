package com.cebolao.lotofacil.domain.service.solver

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.domain.model.FilterRule
import com.cebolao.lotofacil.domain.model.FilterState
import com.cebolao.lotofacil.domain.model.FilterType
import com.cebolao.lotofacil.domain.model.GameComputedMetrics
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.model.toRule
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Solves the constraints using a Backtracking algorithm with Forward Checking (Look-ahead).
 *
 * It efficiently prunes the search space by maintaining count of available properties (evens, primes, etc.)
 * in the remaining numbers ('Suffix Sum' arrays).
 */
class BacktrackingSolver(
    filters: List<FilterState>,
    private val lastDrawNumbers: Set<Int>,
    private val rnd: Random,
    private val onAttempt: () -> Unit,
    private val onRejection: (FilterType) -> Unit
) {

    // Filter bounds (e.g., Sum must be between 180 and 220)
    private val sumBounds: IntRange? = filters.boundFor(FilterType.SOMA_DEZENAS)
    private val evensBounds: IntRange? = filters.boundFor(FilterType.PARES)
    private val primesBounds: IntRange? = filters.boundFor(FilterType.PRIMOS)
    private val fibonacciBounds: IntRange? = filters.boundFor(FilterType.FIBONACCI)
    private val frameBounds: IntRange? = filters.boundFor(FilterType.MOLDURA)
    private val repeatedBounds: IntRange? = filters.boundFor(FilterType.REPETIDAS_CONCURSO_ANTERIOR)
    private val mult3Bounds: IntRange? = filters.boundFor(FilterType.MULTIPLES_OF_3)
    private val centerBounds: IntRange? = filters.boundFor(FilterType.CENTER)

    // General rules for final verification
    private val rules: List<FilterRule> = filters.mapNotNull { it.toRule() }

    // State
    private val currentSelection = IntArray(GameConstants.GAME_SIZE)
    private val candidates = GameConstants.ALL_NUMBERS.toIntArray()
    private var lastSolution: LotofacilGame? = null

    // Pre-computed Look-ahead arrays (Suffix Sums)
    // *_After[n] tells us how many numbers with that property exist in the range (n, 25]
    private val maxN = GameConstants.MAX_NUMBER
    private val evensAfter = IntArray(maxN + 1)
    private val primesAfter = IntArray(maxN + 1)
    private val fibAfter = IntArray(maxN + 1)
    private val frameAfter = IntArray(maxN + 1)
    private val repeatedAfter = IntArray(maxN + 1)
    private val multiples3After = IntArray(maxN + 1)
    private val centerAfter = IntArray(maxN + 1)

    init {
        // Fill look-ahead arrays (Reverse iteration)
        for (n in maxN downTo 0) {
            val next = n + 1
            if (next > maxN) {
                evensAfter[n] = 0
                primesAfter[n] = 0
                fibAfter[n] = 0
                frameAfter[n] = 0
                repeatedAfter[n] = 0
                multiples3After[n] = 0
                centerAfter[n] = 0
            } else {
                evensAfter[n] = evensAfter[next] + if (next % 2 == 0) 1 else 0
                primesAfter[n] = primesAfter[next] + if (GameConstants.PRIMOS.contains(next)) 1 else 0
                fibAfter[n] = fibAfter[next] + if (GameConstants.FIBONACCI.contains(next)) 1 else 0
                frameAfter[n] = frameAfter[next] + if (GameConstants.MOLDURA.contains(next)) 1 else 0
                repeatedAfter[n] = repeatedAfter[next] + if (lastDrawNumbers.contains(next)) 1 else 0
                multiples3After[n] = multiples3After[next] + if (next % 3 == 0) 1 else 0
                centerAfter[n] = centerAfter[next] + if (GameConstants.MIOLO.contains(next)) 1 else 0
            }
        }
    }

    suspend fun findValidGame(): LotofacilGame? {
        lastSolution = null
        shuffleInPlace(candidates, rnd) // Randomize search order to get different results

        // Reset selection
        for (i in currentSelection.indices) currentSelection[i] = 0

        val ok = solve(
            index = 0,
            currentSum = 0,
            currentEvens = 0,
            currentPrimes = 0,
            currentFib = 0,
            currentFrame = 0,
            currentRepeated = 0,
            currentMult3 = 0,
            currentCenter = 0,
            lastNum = 0
        )
        return if (ok) lastSolution else null
    }

    /**
     * Recursive backtracking function.
     *
     * @param index The current position in the game array we are filling (0..14)
     * @param lastNum The number selected in the previous position (to ensure strict ascending order)
     */
    private suspend fun solve(
        index: Int,
        currentSum: Int,
        currentEvens: Int,
        currentPrimes: Int,
        currentFib: Int,
        currentFrame: Int,
        currentRepeated: Int,
        currentMult3: Int,
        currentCenter: Int,
        lastNum: Int
    ): Boolean {
        // PERIODIC CHECK: Ensure we can cancel this heavy computation if the User leaves the screen
        currentCoroutineContext().ensureActive()

        // BASE CASE: All 15 numbers selected
        if (index == GameConstants.GAME_SIZE) {
            return validateCompleteGame(
                sum = currentSum,
                evens = currentEvens,
                primes = currentPrimes,
                fib = currentFib,
                frame = currentFrame,
                repeated = currentRepeated,
                mult3 = currentMult3,
                center = currentCenter
            )
        }

        // PRUNING: Basic count feasibility (Do we have enough numbers left?)
        if (!isBasicFeasible(index, lastNum)) return false

        // PRUNING: Forward Checking (Can we still satisfy the filters with remaining numbers?)
        if (!areBoundsFeasible(currentSum, currentEvens, currentPrimes, currentFib, currentFrame, currentRepeated, currentMult3, currentCenter, index, lastNum)) return false

        // RECURSION: Try next numbers
        return tryNumberSelection(index, currentSum, currentEvens, currentPrimes, currentFib, currentFrame, currentRepeated, currentMult3, currentCenter, lastNum)
    }

    private fun isBasicFeasible(index: Int, lastNum: Int): Boolean {
        val remainingCount = GameConstants.GAME_SIZE - index
        val availableCount = maxN - lastNum
        return availableCount >= remainingCount
    }

    /**
     * Checks if it is mathematically possible to satisfy all bounds given the current partial state.
     * Uses the pre-computed Suffix Sum arrays (*_After) to know the maximum possible increase for each property.
     */
    private fun areBoundsFeasible(
        currentSum: Int,
        currentEvens: Int,
        currentPrimes: Int,
        currentFib: Int,
        currentFrame: Int,
        currentRepeated: Int,
        currentMult3: Int,
        currentCenter: Int,
        index: Int,
        lastNum: Int
    ): Boolean {
        val remainingCount = GameConstants.GAME_SIZE - index
        val availableCount = maxN - lastNum // Total numbers available to pick from

        // Check Sum Constraints
        if (!isSumFeasible(currentSum, lastNum, remainingCount)) return false

        // Check Count Constraints (e.g. Evens, Primes)
        // Logic: current + max_remaining >= min_required  AND  current + min_remaining <= max_allowed
        // For boolean properties, max_remaining = matches_available, min_remaining = 0 (if we pick none) or forced by count

        if (!isCountFeasibleForType(currentEvens, evensBounds, remainingCount, evensAfter[lastNum], availableCount - evensAfter[lastNum])) return false
        if (!isCountFeasibleForType(currentPrimes, primesBounds, remainingCount, primesAfter[lastNum], availableCount - primesAfter[lastNum])) return false
        if (!isCountFeasibleForType(currentFib, fibonacciBounds, remainingCount, fibAfter[lastNum], availableCount - fibAfter[lastNum])) return false
        if (!isCountFeasibleForType(currentFrame, frameBounds, remainingCount, frameAfter[lastNum], availableCount - frameAfter[lastNum])) return false
        if (!isCountFeasibleForType(currentRepeated, repeatedBounds, remainingCount, repeatedAfter[lastNum], availableCount - repeatedAfter[lastNum])) return false
        if (!isCountFeasibleForType(currentMult3, mult3Bounds, remainingCount, multiples3After[lastNum], availableCount - multiples3After[lastNum])) return false
        if (!isCountFeasibleForType(currentCenter, centerBounds, remainingCount, centerAfter[lastNum], availableCount - centerAfter[lastNum])) return false

        return true
    }

    private fun isSumFeasible(currentSum: Int, lastNum: Int, remainingCount: Int): Boolean {
        if (sumBounds == null) return true

        // Min possible sum: picking the smallest available numbers i.e., lastNum+1, lastNum+2...
        val minPossibleSum = currentSum + minSumAfter(lastNum, remainingCount)
        // Max possible sum: picking the largest available numbers i.e., 25, 24, 23...
        val maxPossibleSum = currentSum + maxSumAfter(lastNum, remainingCount)

        return minPossibleSum <= sumBounds.last && maxPossibleSum >= sumBounds.first
    }

    private fun isCountFeasibleForType(
        current: Int,
        bounds: IntRange?,
        remainingCount: Int,
        availableMatch: Int,     // How many remaining numbers HAVE this property
        availableNonMatch: Int   // How many remaining numbers DO NOT HAVE this property
    ): Boolean {
        if (bounds == null) return true
        return isCountFeasible(current, bounds, remainingCount, availableMatch, availableNonMatch)
    }

    private suspend fun tryNumberSelection(
        index: Int,
        currentSum: Int,
        currentEvens: Int,
        currentPrimes: Int,
        currentFib: Int,
        currentFrame: Int,
        currentRepeated: Int,
        currentMult3: Int,
        currentCenter: Int,
        lastNum: Int
    ): Boolean {
        // Iterate through candidates
        for (i in candidates.indices) {
            val num = candidates[i]
            if (num <= lastNum) continue // Ensure ascending order

            // Optimistic assignment (try this number)
            currentSelection[index] = num

            if (solve(
                    index = index + 1,
                    currentSum = currentSum + num,
                    currentEvens = currentEvens + if (num % 2 == 0) 1 else 0,
                    currentPrimes = currentPrimes + if (GameConstants.PRIMOS.contains(num)) 1 else 0,
                    currentFib = currentFib + if (GameConstants.FIBONACCI.contains(num)) 1 else 0,
                    currentFrame = currentFrame + if (GameConstants.MOLDURA.contains(num)) 1 else 0,
                    currentRepeated = currentRepeated + if (lastDrawNumbers.contains(num)) 1 else 0,
                    currentMult3 = currentMult3 + if (num % 3 == 0) 1 else 0,
                    currentCenter = currentCenter + if (GameConstants.MIOLO.contains(num)) 1 else 0,
                    lastNum = num
                )
            ) {
                return true // Solution found!
            }

            // Backtrack: Reset selection (though strictly not needed as we overwrite, but good for debug)
            currentSelection[index] = 0
        }

        return false
    }

    private fun validateCompleteGame(
        sum: Int, evens: Int, primes: Int, fib: Int, frame: Int, repeated: Int, mult3: Int, center: Int
    ): Boolean {
        onAttempt()

        val gameSet = currentSelection.toSet()

        // Final check: Construct full game and check complex rules (like sequences)
        val game = LotofacilGame.fromNumbers(gameSet)

        val metrics = GameComputedMetrics(
            sum = sum,
            evens = evens,
            primes = primes,
            fibonacci = fib,
            frame = frame,
            repeated = repeated,
            sequences = game.sequences,
            multiplesOf3 = mult3,
            center = center
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
        if (start > maxN || end > maxN) return Int.MAX_VALUE / 4 // Overflow protection
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
        if (current > bounds.last) return false // Already exceeded max

        // Max possible we can reach: current + take ALL remaining matches (capped by remaining slots)
        val maxAdditional = min(remainingCount, availableMatch)
        val maxTotal = current + maxAdditional
        if (maxTotal < bounds.first) return false // Impossible to reach min

        // Min possible we can be forced to have: current + (remaining slots - non_matches)
        // i.e., we are forced to take a match if we run out of non-matches
        val minAdditional = max(0, remainingCount - availableNonMatch)
        val minTotal = current + minAdditional
        if (minTotal > bounds.last) return false // Forced to exceed max

        return true
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
