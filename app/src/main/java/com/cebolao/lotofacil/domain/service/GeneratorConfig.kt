package com.cebolao.lotofacil.domain.service

private const val MAX_DIVERSITY_THRESHOLD = 15

/**
 * Configuration for game generation algorithm
 * 
 * @param maxRandomAttempts Maximum random generation attempts before fallback to backtracking
 * @param timeoutMs Maximum time allowed for generation in milliseconds
 * @param batchSize Number of games to generate in each batch for progress reporting
 * @param diversityThreshold Minimum number of different numbers between games
 * @param enableBacktracking Whether to use backtracking solver when random generation fails
 */
data class GeneratorConfig(
    val maxRandomAttempts: Int = 1000,
    val timeoutMs: Long = 20000L,
    val batchSize: Int = 50,
    val diversityThreshold: Int = 14,
    val enableBacktracking: Boolean = true
) {
    init {
        require(maxRandomAttempts > 0) { "maxRandomAttempts must be positive" }
        require(timeoutMs > 0) { "timeoutMs must be positive" }
        require(batchSize > 0) { "batchSize must be positive" }
        require(diversityThreshold in 0..MAX_DIVERSITY_THRESHOLD) { 
            "diversityThreshold must be between 0 and $MAX_DIVERSITY_THRESHOLD" 
        }
    }
    
    companion object {
        /**
         * Balanced configuration (default)
         */
        val BALANCED = GeneratorConfig()
    }
}

