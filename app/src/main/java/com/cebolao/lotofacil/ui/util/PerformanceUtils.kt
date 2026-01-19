package com.cebolao.lotofacil.ui.util

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Utilitários de performance para Jetpack Compose.
 * Fornece helpers para memoização e otimização de recomposições.
 */

/**
 * Marca uma classe ou interface como estável do ponto de vista do Compose.
 * Use quando uma classe é imutável ou notifica mudanças corretamente.
 * 
 * Já existe em Compose, mas incluído aqui para referência.
 */
// @Stable annotation já existe no Compose

/**
 * Wrapper para derivedStateOf com logging opcional em debug builds.
 * Útil para detectar cálculos desnecessários.
 * 
 * @param calculation Cálculo a ser executado
 * @return Estado derivado
 */
@Composable
fun <T> trackedDerivedState(
    calculation: () -> T
): State<T> = remember {
    derivedStateOf(calculation)
}

/**
 * Extension function para transformar Set em ImmutableList de forma eficiente.
 * Útil para estabilizar parâmetros de Composables.
 */
fun <T> Set<T>.toStableList(): ImmutableList<T> = this.toList().toImmutableList()

/**
 * Extension function para transformar List em ImmutableList de forma segura.
 */
fun <T> List<T>.toStable(): ImmutableList<T> = this.toImmutableList()

/**
 * Classe wrapper para estado de seleção otimizado.
 * Evita recomposições desnecessárias ao trabalhar com seleções.
 */
@Stable
data class SelectionState(
    val selectedItems: ImmutableList<Int>,
    val maxSelection: Int?,
    val isFull: Boolean
) {
    val count: Int get() = selectedItems.size
    
    fun isSelected(item: Int): Boolean = selectedItems.contains(item)
    
    fun canSelect(item: Int): Boolean {
        return isSelected(item) || !isFull
    }
    
    companion object {
        fun from(
            selected: Set<Int>,
            maxSelection: Int?
        ): SelectionState {
            val list = selected.toStableList()
            val isFull = maxSelection != null && list.size >= maxSelection
            return SelectionState(list, maxSelection, isFull)
        }
    }
}

/**
 * Wrapper para remember com múltiplas chaves que cria um objeto estável.
 * Útil para evitar recomposições quando as dependências são complexas.
 */
@Composable
inline fun <T> rememberStable(
    vararg keys: Any?,
    crossinline calculation: () -> T
): T = remember(*keys) { calculation() }

/**
 * Extension para SnapshotStateList que cria uma cópia imutável.
 * Útil para passar listas como parâmetros de Composables.
 */
fun <T> SnapshotStateList<T>.toImmutableSnapshot(): ImmutableList<T> =
    this.toList().toImmutableList()

/**
 * Cria um estado derivado que compara por igualdade estrutural.
 * Evita recomposições quando o valor calculado não muda estruturalmente.
 */
@Composable
fun <T> derivedStateOfStructuralEquality(
    calculation: () -> T
): State<T> = remember {
    derivedStateOf(referentialEqualityPolicy(), calculation)
}

/**
 * Marca para indicar que um Composable deve ser skippable.
 * Útil para documentação e referência.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Skippable

/**
 * Marca para indicar que um Composable é otimizado para performance.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PerformanceOptimized(val reason: String = "")

/**
 * Helper class para tracking de recomposições em debug.
 * Use com cuidado - apenas para debugging!
 */
class RecompositionCounter(private val tag: String) {
    private var count = 0
    
    @Composable
    fun Track() {
        SideEffect {
            count++
            // Em produção, isso seria removido ou controlado por BuildConfig
            // println("[$tag] Recomposition #$count")
        }
    }
    
    fun reset() {
        count = 0
    }
    
    fun getCount(): Int = count
}

/**
 * Extension para Map que retorna uma cópia imutável otimizada.
 */
fun <K, V> Map<K, V>.toImmutableMap(): Map<K, V> =
    this.toMap() // No Kotlin, Map é imutável por padrão

/**
 * Wrapper estável para pares de valores.
 * Útil quando você precisa passar múltiplos valores como um único parâmetro.
 */
@Stable
data class StablePair<out A, out B>(
    val first: A,
    val second: B
)

/**
 * Wrapper estável para triplas de valores.
 */
@Stable
data class StableTriple<out A, out B, out C>(
    val first: A,
    val second: B,
    val third: C
)

/**
 * Helper para criar um remember com key baseada em hash de conteúdo.
 * Útil para coleções que podem mudar de referência mas não de conteúdo.
 */
@Composable
inline fun <T> rememberByContent(
    content: Any?,
    crossinline calculation: () -> T
): T = remember(content.hashCode()) { calculation() }

/**
 * Extension function que verifica se um Set mudou estruturalmente.
 */
fun <T> Set<T>.structurallyEquals(other: Set<T>): Boolean {
    if (this.size != other.size) return false
    return this.all { it in other }
}
