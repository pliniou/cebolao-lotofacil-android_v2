package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Base class for all ViewModels.
 *
 * IMPORTANT: do not shadow [androidx.lifecycle.viewModelScope]. Rely on the lifecycle-aware
 * scope provided by AndroidX to avoid leaking coroutines beyond ViewModel lifecycle.
 */
abstract class BaseViewModel : ViewModel()
