package com.cebolao.lotofacil.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.model.ThemeMode
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val DATASTORE_NAME = "user_prefs"
private const val TAG = "UserPrefsRepo"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesRepository {

    private val dataStore = context.dataStore
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val PALETTE = stringPreferencesKey("accent_palette")
    }

    override val themeMode: Flow<ThemeMode> = getValue(Keys.THEME, ThemeMode.SYSTEM.storageValue)
        .map { ThemeMode.fromStorage(it) }
    override val hasCompletedOnboarding: Flow<Boolean> = getValue(Keys.ONBOARDING, false)
    override val accentPalette: Flow<String> = getValue(Keys.PALETTE, "AZUL")

    override suspend fun setThemeMode(mode: ThemeMode) = setValue(Keys.THEME, mode.storageValue)
    override suspend fun setHasCompletedOnboarding(completed: Boolean) = setValue(Keys.ONBOARDING, completed)
    override suspend fun setAccentPalette(paletteName: String) = setValue(Keys.PALETTE, paletteName)

    private fun <T> getValue(key: Preferences.Key<T>, default: T): Flow<T> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Log.e(TAG, "Error reading prefs", e)
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
            Log.e(TAG, "DataStore write error", e)
        }
    }
}
