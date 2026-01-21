package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Base class for all ViewModels.  Provides a [CoroutineScope] with a supervisor job and a
 * custom [CoroutineExceptionHandler] to prevent unhandled exceptions from crashing the app.
 */
abstract class BaseViewModel : ViewModel() {

    /** Supervisor scope used to launch coroutines in derived ViewModels. */
    protected val viewModelScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            // Default exception handling.  Override in subclasses if needed.
            throwable.printStackTrace()
        }
    )

    /** Launches a coroutine safely with the provided body and handles exceptions. */
    protected fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() }.onFailure { it.printStackTrace() } }
    }
}
