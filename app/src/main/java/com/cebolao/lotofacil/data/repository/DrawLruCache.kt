package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.domain.model.Draw
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple LRU (Least Recently Used) cache for Draw entities.
 * Thread-safe implementation using Mutex.
 * 
 * @param maxSize Maximum number of entries to keep in cache
 */
class DrawLruCache(private val maxSize: Int = 100) {
    private val cache = LinkedHashMap<Int, Draw>(maxSize, 0.75f, true)
    private val mutex = Mutex()

    /**
     * Get a draw from cache by contest number
     */
    suspend fun get(contestNumber: Int): Draw? = mutex.withLock {
        cache[contestNumber]
    }

    /**
     * Put a draw in the cache
     */
    suspend fun put(contestNumber: Int, draw: Draw) = mutex.withLock {
        cache[contestNumber] = draw
        
        // Remove oldest entry if cache is full
        if (cache.size > maxSize) {
            val oldest = cache.entries.first()
            cache.remove(oldest.key)
        }
    }

    /**
     * Put multiple draws in the cache
     */
    suspend fun putAll(draws: List<Draw>) = mutex.withLock {
        draws.forEach { draw ->
            cache[draw.contestNumber] = draw
        }
        
        // Trim cache if it exceeds maxSize
        while (cache.size > maxSize) {
            val oldest = cache.entries.first()
            cache.remove(oldest.key)
        }
    }

    /**
     * Get all cached draws as a list
     */
    suspend fun getAll(): List<Draw> = mutex.withLock {
        cache.values.toList()
    }

    /**
     * Check if cache contains a specific contest number
     */
    suspend fun contains(contestNumber: Int): Boolean = mutex.withLock {
        cache.containsKey(contestNumber)
    }

    /**
     * Clear the entire cache
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    /**
     * Get current cache size
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }
}
