package com.cebolao.lotofacil.ui.components.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Performance optimization utilities for Jetpack Compose LazyColumn and LazyRow.
 * 
 * This file contains helpers and best practices for optimizing lazy layouts,
 * reducing recompositions, and ensuring smooth scrolling performance.
 */

/**
 * Creates and remembers stable PaddingValues for lazy layouts.
 * 
 * PaddingValues should be cached to avoid unnecessary recompositions of the lazy layout.
 * This is especially important when padding is calculated from multiple sources.
 * 
 * Example:
 * ```kotlin
 * val contentPadding = rememberStablePadding(
 *     top = 16.dp,
 *     bottom = scaffoldPadding.calculateBottomPadding() + 24.dp,
 *     horizontal = 16.dp
 * )
 * LazyColumn(contentPadding = contentPadding) { ... }
 * ```
 */
@Composable
fun rememberStablePadding(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
    start: Dp = 0.dp,
    end: Dp = 0.dp
): PaddingValues = remember(top, bottom, start, end) {
    PaddingValues(start = start, top = top, end = end, bottom = bottom)
}

/**
 * Creates and remembers stable PaddingValues with horizontal/vertical convenience parameters.
 */
@Composable
fun rememberStablePadding(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp
): PaddingValues = remember(horizontal, vertical) {
    PaddingValues(horizontal = horizontal, vertical = vertical)
}

/**
 * Creates and remembers uniform PaddingValues.
 */
@Composable
fun rememberStablePadding(all: Dp): PaddingValues = remember(all) {
    PaddingValues(all = all)
}

/**
 * Best practices for LazyColumn/LazyRow performance:
 * 
 * ## 1. Always use `key` parameter
 * ```kotlin
 * items(
 *     items = myList,
 *     key = { item -> item.id } // Stable, unique key
 * ) { item -> ... }
 * ```
 * 
 * ## 2. Use `contentType` for different item types
 * ```kotlin
 * items(
 *     items = myList,
 *     key = { it.id },
 *     contentType = { "my_item_type" }
 * ) { item -> ... }
 * ```
 * 
 * ## 3. Cache expensive computations with `remember`
 * ```kotlin
 * val processedData = remember(rawData) {
 *     rawData.map { /* expensive transformation */ }
 * }
 * ```
 * 
 * ## 4. Use `derivedStateOf` for computed values that depend on frequently changing state
 * ```kotlin
 * val filteredItems by remember {
 *     derivedStateOf {
 *         items.filter { it.matches(searchQuery) }
 *     }
 * }
 * ```
 * 
 * ## 5. Mark data classes as @Immutable
 * ```kotlin
 * @Immutable
 * data class MyItem(val id: String, val name: String)
 * ```
 * 
 * ## 6. Use ImmutableList for list properties
 * ```kotlin
 * import kotlinx.collections.immutable.ImmutableList
 * 
 * @Immutable
 * data class MyState(val items: ImmutableList<MyItem>)
 * ```
 * 
 * ## 7. Extract callbacks with remember to avoid recreating lambdas
 * ```kotlin
 * val onItemClick = remember(viewModel) {
 *     { item: MyItem -> viewModel.handleClick(item) }
 * }
 * ```
 * 
 * ## 8. Use rememberUpdatedState for callback parameters that change frequently
 * ```kotlin
 * val currentOnClick by rememberUpdatedState(onClick)
 * ```
 */
object LazyLayoutBestPractices {
    /**
     * Generates a stable key for items that don't have a unique ID.
     * Use this as a last resort - prefer using actual unique IDs when available.
     */
    fun <T> generateStableKey(item: T, index: Int): String {
        return "${item.hashCode()}_$index"
    }
}

/**
 * Marker annotation for composables that are optimized for lazy layouts.
 * This is a documentation annotation to indicate that performance best practices
 * have been applied to this composable.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class LazyLayoutOptimized(
    val description: String = ""
)
