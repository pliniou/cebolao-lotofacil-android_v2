package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.domain.usecase.ObserveAppConfigUseCase
import com.cebolao.lotofacil.domain.usecase.UpdateAppConfigUseCase
import com.cebolao.lotofacil.navigation.HomeRoute
import com.cebolao.lotofacil.navigation.OnboardingRoute
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the main application screen.
 */
data class MainUiState(
    val isReady: Boolean = false
)

/**
 * State representing the initial navigation destination.
 */
data class StartDestinationState(
    val destination: Any = OnboardingRoute,
    val isLoading: Boolean = true
)

/**
 * ViewModel for the main application activity.
 * Manages app-level configuration including theme, accent palette, and onboarding state.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeAppConfigUseCase: ObserveAppConfigUseCase,
    private val updateAppConfigUseCase: UpdateAppConfigUseCase
) : ViewModel() {

    private val paletteByName: Map<String, AccentPalette> =
        AccentPalette.entries.associateBy { it.name }

    private val onboardingCompleted = observeAppConfigUseCase.hasCompletedOnboarding

    /**
     * StateFlow that determines the initial navigation destination based on onboarding status.
     */
    val startDestination: StateFlow<StartDestinationState> = onboardingCompleted
        .map { completed ->
            val route = if (completed) HomeRoute else OnboardingRoute
            StartDestinationState(destination = route, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = StartDestinationState(isLoading = true)
        )

    /**
     * StateFlow indicating whether the app is ready to be displayed.
     */
    val uiState: StateFlow<MainUiState> = startDestination
        .map { MainUiState(isReady = !it.isLoading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = MainUiState(isReady = false)
        )

    /**
     * StateFlow of the current theme mode (light/dark/system).
     */
    val themeMode: StateFlow<String> = observeAppConfigUseCase.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = THEME_MODE_LIGHT
        )

    /**
     * StateFlow of the current accent color palette.
     */
    val accentPalette: StateFlow<AccentPalette> = observeAppConfigUseCase.accentPalette
        .map(::mapAccentPalette)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = AccentPalette.AZUL
        )

    /**
     * Single entry point for UI events.
     */
    fun onEvent(event: MainUiEvent) {
        viewModelScope.launch {
            when (event) {
                is MainUiEvent.CompleteOnboarding -> {
                    updateAppConfigUseCase.completeOnboarding()
                }
                is MainUiEvent.SetThemeMode -> {
                    updateAppConfigUseCase.setThemeMode(event.mode)
                }
                is MainUiEvent.SetAccentPalette -> {
                    updateAppConfigUseCase.setAccentPalette(event.palette.name)
                }
            }
        }
    }

    private fun mapAccentPalette(name: String): AccentPalette {
        return paletteByName[name] ?: AccentPalette.AZUL
    }
}

/**
 * UI Events for MainViewModel.
 */
sealed interface MainUiEvent {
    data object CompleteOnboarding : MainUiEvent
    data class SetThemeMode(val mode: String) : MainUiEvent
    data class SetAccentPalette(val palette: AccentPalette) : MainUiEvent
}
