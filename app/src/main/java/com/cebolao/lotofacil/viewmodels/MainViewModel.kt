package com.cebolao.lotofacil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.domain.usecase.ObserveAppConfigUseCase
import com.cebolao.lotofacil.domain.usecase.UpdateAppConfigUseCase
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isReady: Boolean = false
)

data class StartDestinationState(
    val destination: String = Screen.Onboarding.route,
    val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    observeAppConfigUseCase: ObserveAppConfigUseCase,
    private val updateAppConfigUseCase: UpdateAppConfigUseCase
) : ViewModel() {

    private val paletteByName: Map<String, AccentPalette> =
        AccentPalette.entries.associateBy { it.name }

    private val onboardingCompleted = observeAppConfigUseCase.hasCompletedOnboarding

    val startDestination: StateFlow<StartDestinationState> = onboardingCompleted
        .map { completed ->
            val route = if (completed) Screen.Home.route else Screen.Onboarding.route
            StartDestinationState(destination = route, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = StartDestinationState(isLoading = true)
        )

    val uiState: StateFlow<MainUiState> = startDestination
        .map { MainUiState(isReady = !it.isLoading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = MainUiState(isReady = false)
        )

    val themeMode: StateFlow<String> = observeAppConfigUseCase.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = THEME_MODE_LIGHT
        )

    val accentPalette: StateFlow<AccentPalette> = observeAppConfigUseCase.accentPalette
        .map(::mapAccentPalette)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = AccentPalette.AZUL
        )

    fun onOnboardingComplete() {
        viewModelScope.launch {
            updateAppConfigUseCase.completeOnboarding()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            updateAppConfigUseCase.setThemeMode(mode)
        }
    }

    fun setAccentPalette(palette: AccentPalette) {
        viewModelScope.launch {
            updateAppConfigUseCase.setAccentPalette(palette.name)
        }
    }

    private fun mapAccentPalette(name: String): AccentPalette {
        return paletteByName[name] ?: AccentPalette.AZUL
    }
}
