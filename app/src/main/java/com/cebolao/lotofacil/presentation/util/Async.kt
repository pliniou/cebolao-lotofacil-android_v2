package com.cebolao.lotofacil.presentation.util

import androidx.annotation.StringRes

/**
 * A sealed interface representing the state of an asynchronous operation.
 * Used to wrap data in UI states.
 */
sealed interface Async<out T> {
    data object Loading : Async<Nothing>
    data class Success<out T>(val data: T) : Async<T>
    data class Error(@param:StringRes val messageRes: Int, val throwable: Throwable? = null) : Async<Nothing>
    data object Uninitialized : Async<Nothing>
}

/**
 * Returns the data if the state is Success, otherwise null.
 */
fun <T> Async<T>.invoke(): T? = (this as? Async.Success)?.data

/**
 * Returns true if the state is Loading.
 */
val Async<*>.isLoading: Boolean
    get() = this is Async.Loading
