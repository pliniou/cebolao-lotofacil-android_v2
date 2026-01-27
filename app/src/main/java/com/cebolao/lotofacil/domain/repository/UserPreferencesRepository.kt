package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /**
     * Current theme (string according to app standard).
     */
    val themeMode: Flow<ThemeMode>

    /**
     * Indicates if onboarding has been completed.
     */
    val hasCompletedOnboarding: Flow<Boolean>

    /**
     * Selected accent palette (by name).
     */
    val accentPalette: Flow<String>

    /**
     * Sets the theme mode.
     */
    suspend fun setThemeMode(mode: ThemeMode)

    /**
     * Sets onboarding completion.
     */
    suspend fun setHasCompletedOnboarding(completed: Boolean)

    /**
     * Sets the accent palette.
     */
    suspend fun setAccentPalette(paletteName: String)
}
