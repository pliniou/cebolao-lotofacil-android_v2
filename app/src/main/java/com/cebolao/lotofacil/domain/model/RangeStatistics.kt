package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable

/**
 * Estatísticas completas de análise por faixa de acertos.
 * Calculado usando todo o banco de dados disponível, não apenas últimos 20 concursos.
 */
@Immutable
data class RangeStatistics(
    val totalDraws: Int,
    val range0to5: Int,      // 0-5 acertos
    val range6to10: Int,     // 6-10 acertos  
    val range11to15: Int,    // 11-15 acertos
    val range16to20: Int,    // 16-20 acertos
    val averageHits: Float,   // Média de acertos por jogo
    val standardDeviation: Float, // Desvio padrão dos acertos
    val mostFrequentRange: String, // Faixa mais frequente
    val leastFrequentRange: String // Faixa menos frequente
) {
    /**
     * Calcula a porcentagem de jogos em uma determinada faixa
     */
    fun getRangePercentage(rangeCount: Int): Float {
        return if (totalDraws > 0) (rangeCount.toFloat() / totalDraws * 100) else 0f
    }
    
    /**
     * Retorna a faixa com maior ocorrência
     */
    fun getDominantRange(): String {
        val ranges = mapOf(
            "0-5" to range0to5,
            "6-10" to range6to10,
            "11-15" to range11to15,
            "16-20" to range16to20
        )
        return ranges.maxByOrNull { it.value }?.key ?: "N/A"
    }
    
    /**
     * Verifica se um número de acertos está acima da média
     */
    fun isAboveAverage(hits: Int): Boolean {
        return hits > averageHits
    }
    
    /**
     * Classifica o desempenho baseado na faixa de acertos
     */
    fun classifyPerformance(hits: Int): PerformanceLevel {
        return when {
            hits >= 16 -> PerformanceLevel.EXCELLENT
            hits >= 11 -> PerformanceLevel.GOOD
            hits >= 6 -> PerformanceLevel.AVERAGE
            hits >= 1 -> PerformanceLevel.BELOW_AVERAGE
            else -> PerformanceLevel.POOR
        }
    }
}

/**
 * Níveis de desempenho baseados na faixa de acertos
 */
enum class PerformanceLevel {
    POOR,
    BELOW_AVERAGE,
    AVERAGE,
    GOOD,
    EXCELLENT
}

/**
 * Utilitário para calcular estatísticas de faixa a partir de dados históricos
 */
object RangeStatisticsCalculator {
    
    /**
     * Calcula estatísticas completas de faixa a partir de uma lista de jogos
     */
    fun calculateFromGames(games: List<LotofacilGame>): RangeStatistics {
        if (games.isEmpty()) {
            return RangeStatistics(0, 0, 0, 0, 0, 0f, 0f, "N/A", "N/A")
        }
        
        val totalDraws = games.size
        
        // Contar acertos por faixa
        var range0to5 = 0
        var range6to10 = 0
        var range11to15 = 0
        var range16to20 = 0
        
        val hitsList = mutableListOf<Int>()
        
        games.forEach { game ->
            val hits = game.numbers.size
            hitsList.add(hits)
            
            when {
                hits <= 5 -> range0to5++
                hits <= 10 -> range6to10++
                hits <= 15 -> range11to15++
                else -> range16to20++
            }
        }
        
        // Calcular estatísticas
        val averageHits = hitsList.average().toFloat()
        val variance = hitsList.map { (it - averageHits).pow(2) }.average()
        val standardDeviation = kotlin.math.sqrt(variance).toFloat()
        
        val ranges = mapOf(
            "0-5" to range0to5,
            "6-10" to range6to10,
            "11-15" to range11to15,
            "16-20" to range16to20
        )
        
        val mostFrequentRange = ranges.maxByOrNull { it.value }?.key ?: "N/A"
        val leastFrequentRange = ranges.minByOrNull { it.value }?.key ?: "N/A"
        
        return RangeStatistics(
            totalDraws = totalDraws,
            range0to5 = range0to5,
            range6to10 = range6to10,
            range11to15 = range11to15,
            range16to20 = range16to20,
            averageHits = averageHits,
            standardDeviation = standardDeviation,
            mostFrequentRange = mostFrequentRange,
            leastFrequentRange = leastFrequentRange
        )
    }
    
    /**
     * Calcula estatísticas a partir de dados de check report
     */
    fun calculateFromCheckReports(reports: List<CheckReport>): RangeStatistics {
        val games = reports.map { it.ticket }
        return calculateFromGames(games)
    }
}
