package com.cebolao.lotofacil.domain.cache

import com.cebolao.lotofacil.domain.model.StatisticsReport
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache simples para estatísticas calculadas
 * Evita recálculo desnecessário de estatísticas quando os parâmetros são os mesmos
 */
@Singleton
class StatisticsCache @Inject constructor() {
    
    private data class CacheKey(
        val timeWindow: Int,
        val historySize: Int
    )
    
    private val cache = ConcurrentHashMap<CacheKey, CachedStats>()
    private val mutex = Mutex()
    
    private data class CachedStats(
        val stats: StatisticsReport,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Duração do cache em milissegundos (5 minutos)
     */
    private val cacheDuration = 5 * 60 * 1000L
    
    /**
     * Obtém estatísticas do cache ou retorna null se não estiver em cache ou expirado
     */
    suspend fun get(timeWindow: Int, historySize: Int): StatisticsReport? = mutex.withLock {
        val key = CacheKey(timeWindow, historySize)
        val cached = cache[key]
        
        if (cached != null) {
            val age = System.currentTimeMillis() - cached.timestamp
            if (age < cacheDuration) {
                return cached.stats
            } else {
                cache.remove(key)
            }
        }
        return null
    }

    /**
     * Retorna o tamanho atual do cache
     */
    fun size(): Int = cache.size
}
