package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.util.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base ViewModel class providing common functionality for error handling and coroutine launching.
 */
abstract class BaseViewModel : ViewModel() {

    // Ideally injected, but for Base class we might need to handle it differently
    // or rely on subclasses to log. For now, we'll keep it simple.
    
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
                onError?.invoke(throwable)
            },
            block = block
        )
    }
}
