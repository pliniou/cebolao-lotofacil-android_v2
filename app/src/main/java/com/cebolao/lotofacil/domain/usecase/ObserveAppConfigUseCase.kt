package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppConfigUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val themeMode: Flow<String> get() = userPreferencesRepository.themeMode
    val hasCompletedOnboarding: Flow<Boolean> get() = userPreferencesRepository.hasCompletedOnboarding
    val accentPalette: Flow<String> get() = userPreferencesRepository.accentPalette
}
