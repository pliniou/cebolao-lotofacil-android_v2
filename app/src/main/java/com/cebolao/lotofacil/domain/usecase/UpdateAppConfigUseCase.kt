package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.model.ThemeMode
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateAppConfigUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun completeOnboarding() {
        userPreferencesRepository.setHasCompletedOnboarding(true)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        userPreferencesRepository.setThemeMode(mode)
    }

    suspend fun setAccentPalette(paletteName: String) {
        userPreferencesRepository.setAccentPalette(paletteName)
    }
}
