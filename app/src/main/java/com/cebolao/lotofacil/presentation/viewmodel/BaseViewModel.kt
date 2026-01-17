package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Base ViewModel class providing common functionality for error handling and coroutine launching.
 */
abstract class BaseViewModel : ViewModel() {
    
    /**
     * Launches a coroutine with a default error handler that logs the exception.
     * @param onError Optional callback to handle the error in the UI (e.g., show a snackbar).
     * @param block The suspend block to execute.
     */
    protected fun launchCatching(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return viewModelScope.launch(
            context = CoroutineExceptionHandler { _, throwable ->
                // Log globally if possible, or trigger onError
                if (onError != null) {
                    onError(throwable)
                } else {
                    Log.e("BaseViewModel", "Uncaught exception in ViewModel", throwable)
                }
            },
            block = block
        )
    }
}
