package com.cebolao.lotofacil.util

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Extension function to launch a coroutine with error handling.
 * This is a utility function to replace the missing launchCatching function.
 */
fun CoroutineScope.launchCatching(
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit
): Job {
    return launch {
        try {
            block()
        } catch (e: Throwable) {
            onError(e)
        }
    }
}
