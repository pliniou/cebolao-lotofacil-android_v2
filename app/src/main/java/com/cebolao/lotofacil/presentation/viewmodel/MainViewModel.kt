package com.cebolao.lotofacil.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.domain.model.ThemeMode
import com.cebolao.lotofacil.domain.usecase.ObserveAppConfigUseCase
import com.cebolao.lotofacil.domain.usecase.UpdateAppConfigUseCase
import com.cebolao.lotofacil.navigation.AppRoute
import com.cebolao.lotofacil.presentation.util.UiEvent
import com.cebolao.lotofacil.presentation.util.UiState
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.DefaultAccentPalette
import com.cebolao.lotofacil.ui.theme.accentPaletteByName
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.util.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * UI state for the main application screen.
 */
@androidx.compose.runtime.Immutable
data class MainUiState(
    val isReady: Boolean = false,
    val startDestination: AppRoute = AppRoute.Onboarding,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentPalette: AccentPalette = DefaultAccentPalette
) : UiState

/**
 * ViewModel for the main application activity.
 * Manages app-level configuration including theme, accent palette, and onboarding state.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeAppConfigUseCase: ObserveAppConfigUseCase,
    private val updateAppConfigUseCase: UpdateAppConfigUseCase
) : BaseViewModel() {

    private val onboardingCompleted = observeAppConfigUseCase.hasCompletedOnboarding
    private val themeMode = observeAppConfigUseCase.themeMode
    private val accentPalette = observeAppConfigUseCase.accentPalette

    /**
     * StateFlow that determines the initial navigation destination based on onboarding status.
     */
    val uiState: StateFlow<MainUiState> = combine(
        onboardingCompleted,
        themeMode,
        accentPalette
    ) { completed, theme, paletteName ->
        val route = if (completed) AppRoute.Home else AppRoute.Onboarding
        MainUiState(
            isReady = true,
            startDestination = route,
            themeMode = theme,
            accentPalette = mapAccentPalette(paletteName)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        initialValue = MainUiState()
    )

    /**
     * Single entry point for UI events.
     */
    fun onEvent(event: MainUiEvent) {
        viewModelScope.launchCatching {
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
        return accentPaletteByName(name)
    }
}

/**
 * UI Events for MainViewModel.
 */
sealed interface MainUiEvent : UiEvent {
    data object CompleteOnboarding : MainUiEvent
    data class SetThemeMode(val mode: ThemeMode) : MainUiEvent
    data class SetAccentPalette(val palette: AccentPalette) : MainUiEvent
}
