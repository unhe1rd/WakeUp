package com.bananchiki.wakeup.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeSettings {
    SYSTEM, LIGHT, DARK
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class ThemePreferenceManager(private val context: Context) {

    private val themeKey = intPreferencesKey("theme_setting")

    val themeFlow: Flow<ThemeSettings> = context.dataStore.data.map { preferences ->
        val themeOrdinal = preferences[themeKey] ?: ThemeSettings.SYSTEM.ordinal
        ThemeSettings.values().getOrElse(themeOrdinal) { ThemeSettings.SYSTEM }
    }

    suspend fun saveTheme(theme: ThemeSettings) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.ordinal
        }
    }
}
