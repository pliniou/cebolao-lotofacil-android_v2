package com.cebolao.lotofacil.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /**
     * Tema atual (string conforme o padrão do app).
     */
    val themeMode: Flow<String>

    /**
     * Indica se o onboarding já foi concluído.
     */
    val hasCompletedOnboarding: Flow<Boolean>

    /**
     * Paleta/accent selecionado (por nome).
     */
    val accentPalette: Flow<String>

    /**
     * Histórico (strings) utilizado por funcionalidades dinâmicas do app.
     */
    suspend fun getHistory(): Set<String>

    /**
     * Adiciona entradas ao histórico dinâmico.
     */
    suspend fun addDynamicHistoryEntries(newHistoryEntries: Set<String>)

    /**
     * Define o modo de tema.
     */
    suspend fun setThemeMode(mode: String)

    /**
     * Define conclusão do onboarding.
     */
    suspend fun setHasCompletedOnboarding(completed: Boolean)

    /**
     * Define a paleta/accent.
     */
    suspend fun setAccentPalette(paletteName: String)
}
