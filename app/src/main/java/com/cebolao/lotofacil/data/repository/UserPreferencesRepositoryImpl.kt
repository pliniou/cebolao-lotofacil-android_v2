package com.cebolao.lotofacil.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import com.cebolao.lotofacil.domain.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val DATASTORE_NAME = "user_prefs"
private const val TAG = "UserPrefsRepo"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)
const val THEME_MODE_LIGHT = "light"
const val THEME_MODE_DARK = "dark"

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logger: Logger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesRepository {

    private val dataStore = context.dataStore
    private object Keys {
        val HISTORY = stringSetPreferencesKey("dynamic_history")
        val THEME = stringPreferencesKey("theme_mode")
        val ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val PALETTE = stringPreferencesKey("accent_palette")
    }

    override val themeMode: Flow<String> = getValue(Keys.THEME, THEME_MODE_DARK)
    override val hasCompletedOnboarding: Flow<Boolean> = getValue(Keys.ONBOARDING, false)
    override val accentPalette: Flow<String> = getValue(Keys.PALETTE, "AZUL")

    override suspend fun setThemeMode(mode: String) = setValue(Keys.THEME, mode)
    override suspend fun setHasCompletedOnboarding(completed: Boolean) = setValue(Keys.ONBOARDING, completed)
    override suspend fun setAccentPalette(paletteName: String) = setValue(Keys.PALETTE, paletteName)
    override suspend fun getHistory(): Set<String> = withContext(ioDispatcher) {
        try {
            dataStore.data.first()[Keys.HISTORY] ?: emptySet()
        } catch (e: IOException) {
            logger.error(TAG, "Error fetching history from DataStore", e)
            emptySet()
        }
    }

    override suspend fun addDynamicHistoryEntries(newHistoryEntries: Set<String>) = withContext(ioDispatcher) {
        val validEntries = newHistoryEntries.filter { it.isNotBlank() }.toSet()
        if (validEntries.isEmpty()) return@withContext

        safeEdit { prefs ->
            val current = prefs[Keys.HISTORY] ?: emptySet()
            prefs[Keys.HISTORY] = current + validEntries
        }
    }

    private fun <T> getValue(key: Preferences.Key<T>, default: T): Flow<T> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                logger.error(TAG, "Error reading prefs", e)
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { it[key] ?: default }

    private suspend fun <T> setValue(key: Preferences.Key<T>, value: T) = withContext(ioDispatcher) {
        safeEdit { it[key] = value }
    }

    private suspend fun safeEdit(action: (MutablePreferences) -> Unit) {
        try {
            dataStore.edit(action)
        } catch (e: IOException) {
            logger.error(TAG, "DataStore write error", e)
        }
    }
}
