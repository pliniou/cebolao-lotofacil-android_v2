package com.cebolao.lotofacil.ui.components.common

import androidx.compose.runtime.Stable
import androidx.compose.runtime.Immutable

/**
 * Stability annotations for Compose performance optimization.
 * 
 * Use @Stable for:
 * - Classes with stable properties that can change over time
 * - Functions that return the same result for the same inputs
 * - Objects that are safe to cache in Compose
 * 
 * Use @Immutable for:
 * - Data classes with immutable properties
 * - Objects that never change after creation
 * - Collections that are immutable
 */

@Stable
interface StableComponent {
    // Marker interface for stable components
}

@Stable
data class StableState<T>(
    val value: T
)

@Stable
data class StableCallback<T>(
    val callback: (T) -> Unit
)

@Stable
data class StablePair<A, B>(
    val first: A,
    val second: B
)

@Stable
data class StableTriple<A, B, C>(
    val first: A,
    val second: B,
    val third: C
)

@Stable
data class StableList<T>(
    val items: List<T>
)

@Stable
data class StableMap<K, V>(
    val entries: Map<K, V>
)

/**
 * Utility functions for creating stable collections
 */
object StableCollections {
    fun <T> stableListOf(vararg items: T): StableList<T> = StableList(items.toList())
    fun <K, V> stableMapOf(vararg pairs: Pair<K, V>): StableMap<K, V> = StableMap(pairs.toMap())
    fun <A, B> stablePairOf(first: A, second: B): StablePair<A, B> = StablePair(first, second)
    fun <A, B, C> stableTripleOf(first: A, second: B, third: C): StableTriple<A, B, C> = StableTriple(first, second, third)
}

/**
 * Performance optimization utilities for Compose
 */
object ComposePerformance {
    
    /**
     * Creates a stable key for LazyColumn/LazyRow items
     */
    inline fun <T> stableKey(item: T, crossinline keyBuilder: (T) -> String): String {
        return keyBuilder(item)
    }
    
    /**
     * Creates a stable callback that won't cause unnecessary recompositions
     */
    inline fun <T> stableCallback(noinline callback: (T) -> Unit): StableCallback<T> {
        return StableCallback(callback)
    }
    
    /**
     * Creates a stable state wrapper
     */
    fun <T> stableState(value: T): StableState<T> = StableState(value)
}

/**
 * Extension functions for converting standard collections to stable wrappers
 */
fun <T> List<T>.toStable(): StableList<T> = StableList(this)
fun <K, V> Map<K, V>.toStable(): StableMap<K, V> = StableMap(this)
