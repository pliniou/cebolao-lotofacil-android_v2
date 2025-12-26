package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.GameConstants
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.Draw
import com.cebolao.lotofacil.domain.model.NumberFrequency
import com.cebolao.lotofacil.domain.model.StatisticsReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

private const val TOP_NUMBERS_COUNT = 25
private const val CACHE_SIZE = 50

// Histogram sizes
private const val HIST_SIZE_EVENS = 16
private const val HIST_SIZE_PRIMES = 16
private const val HIST_SIZE_FRAME = 17
private const val HIST_SIZE_FIB = 16
private const val HIST_SIZE_SUM = 300
private const val HIST_SIZE_LINES = 6
private const val HIST_SIZE_COLS = 6
private const val HIST_SIZE_SEQ = 16
private const val HIST_SIZE_QUAD = 5

@Suppress("SameParameterValue")
@Singleton
class StatisticsAnalyzer @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    // Cache LRU sincronizado (mantém compatibilidade e evita condições de corrida simples)
    private val analysisCache = Collections.synchronizedMap(
        object : LinkedHashMap<String, StatisticsReport>(CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, StatisticsReport>?): Boolean {
                return size > CACHE_SIZE
            }
        }
    )

    suspend fun analyze(draws: List<Draw>, timeWindow: Int = 0): StatisticsReport =
        withContext(defaultDispatcher) {
            if (draws.isEmpty()) return@withContext StatisticsReport()

            val drawsToAnalyze = if (timeWindow > 0) draws.take(timeWindow) else draws
            if (drawsToAnalyze.isEmpty()) return@withContext StatisticsReport()

            val cacheKey =
                "${drawsToAnalyze.size}_${drawsToAnalyze.first().contestNumber}_${drawsToAnalyze.last().contestNumber}"

            analysisCache[cacheKey]?.let { return@withContext it }

            val report = calculateStatistics(drawsToAnalyze)
            analysisCache[cacheKey] = report
            report
        }

    private fun calculateStatistics(draws: List<Draw>): StatisticsReport {
        val frequencies = IntArray(GameConstants.MAX_NUMBER + 1)
        val lastSeen = IntArray(GameConstants.MAX_NUMBER + 1) { -1 }
        val evensDist = IntArray(HIST_SIZE_EVENS)
        val primesDist = IntArray(HIST_SIZE_PRIMES)
        val frameDist = IntArray(HIST_SIZE_FRAME)
        val fibonacciDist = IntArray(HIST_SIZE_FIB)
        val sumDist = IntArray(HIST_SIZE_SUM)
        val linesDist = IntArray(HIST_SIZE_LINES)
        val colsDist = IntArray(HIST_SIZE_COLS)
        val seqDist = IntArray(HIST_SIZE_SEQ)
        val quadDist = IntArray(HIST_SIZE_QUAD)

        var sumAccumulator = 0L

        val latestContest = draws.first().contestNumber
        val oldestContest = draws.last().contestNumber
        val unseenOverdueValue = (latestContest - oldestContest + 1).coerceAtLeast(0)

        for (draw in draws) {
            com.cebolao.lotofacil.domain.model.MaskUtils.forEachNumber(draw.mask) { num ->
                frequencies.safeIncrement(num)
                if (lastSeen[num] == -1) {
                    lastSeen[num] = draw.contestNumber
                }
            }

            evensDist.safeIncrement(draw.evens)
            primesDist.safeIncrement(draw.primes)
            frameDist.safeIncrement(draw.frame)
            fibonacciDist.safeIncrement(draw.fibonacci)
            linesDist.safeIncrement(draw.lines)
            colsDist.safeIncrement(draw.columns)
            seqDist.safeIncrement(draw.sequences)
            quadDist.safeIncrement(draw.quadrants)

            val sumBucket = (draw.sum / 10) * 10
            sumDist.safeIncrement(sumBucket)

            sumAccumulator += draw.sum
        }

        val mostFrequent = createMostFrequentList(frequencies, TOP_NUMBERS_COUNT)
        val mostOverdue = createMostOverdueList(lastSeen, TOP_NUMBERS_COUNT, latestContest, unseenOverdueValue)

        return StatisticsReport(
            mostFrequentNumbers = mostFrequent,
            mostOverdueNumbers = mostOverdue,
            evenDistribution = evensDist.toNonZeroMap(),
            primeDistribution = primesDist.toNonZeroMap(),
            frameDistribution = frameDist.toNonZeroMap(),
            fibonacciDistribution = fibonacciDist.toNonZeroMap(),
            sumDistribution = sumDist.toNonZeroMap(),
            linesDistribution = linesDist.toNonZeroMap(),
            columnsDistribution = colsDist.toNonZeroMap(),
            sequencesDistribution = seqDist.toNonZeroMap(),
            quadrantsDistribution = quadDist.toNonZeroMap(),
            averageSum = if (draws.isNotEmpty()) sumAccumulator.toFloat() / draws.size else 0f,
            totalDrawsAnalyzed = draws.size
        )
    }

    private fun IntArray.safeIncrement(index: Int) {
        if (index in indices) this[index]++
    }

    private fun IntArray.toNonZeroMap(): Map<Int, Int> {
        val map = HashMap<Int, Int>(this.size)
        for (i in indices) {
            val v = this[i]
            if (v > 0) map[i] = v
        }
        return map
    }

    private fun createMostFrequentList(counts: IntArray, limit: Int): List<NumberFrequency> {
        val list = ArrayList<NumberFrequency>(GameConstants.MAX_NUMBER)
        for (num in 1..GameConstants.MAX_NUMBER) {
            list.add(NumberFrequency(num, counts[num]))
        }
        // Ordenação estável: desc por frequência, asc por número
        list.sortWith(compareByDescending<NumberFrequency> { it.frequency }.thenBy { it.number })
        return list.take(limit)
    }

    private fun createMostOverdueList(
        lastSeen: IntArray,
        limit: Int,
        currentContest: Int,
        unseenOverdueValue: Int
    ): List<NumberFrequency> {
        val list = ArrayList<NumberFrequency>(GameConstants.MAX_NUMBER)
        for (num in 1..GameConstants.MAX_NUMBER) {
            val overdue = if (lastSeen[num] == -1) {
                unseenOverdueValue
            } else {
                currentContest - lastSeen[num]
            }
            list.add(NumberFrequency(num, overdue))
        }
        list.sortWith(compareByDescending<NumberFrequency> { it.frequency }.thenBy { it.number })
        return list.take(limit)
    }
}
