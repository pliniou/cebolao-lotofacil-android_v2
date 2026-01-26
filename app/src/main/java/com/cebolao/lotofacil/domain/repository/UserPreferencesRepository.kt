package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /**
     * Tema atual (string conforme o padrão do app).
     */
    val themeMode: Flow<ThemeMode>

    /**
     * Indica se o onboarding já foi concluído.
     */
    val hasCompletedOnboarding: Flow<Boolean>

    /**
     * Paleta/accent selecionado (por nome).
     */
    val accentPalette: Flow<String>

    /**
     * Define o modo de tema.
     */
    suspend fun setThemeMode(mode: ThemeMode)

    /**
     * Define conclusão do onboarding.
     */
    suspend fun setHasCompletedOnboarding(completed: Boolean)

    /**
     * Define a paleta/accent.
     */
    suspend fun setAccentPalette(paletteName: String)
}
