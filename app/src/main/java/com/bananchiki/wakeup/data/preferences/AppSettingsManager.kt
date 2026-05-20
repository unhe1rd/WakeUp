package com.bananchiki.wakeup.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bananchiki.wakeup.data.model.AlarmSound
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

class OnboardingPreferenceManager(private val context: Context) {

    private val onboardingKey = booleanPreferencesKey("onboarding_setting")

    val onboardingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[onboardingKey] ?: true
    }

    suspend fun saveOnboarding(){
        context.dataStore.edit { preferences ->
            preferences[onboardingKey] = false
        }
    }

}

class RingtonePreferenceManager(private val context: Context) {

    private val ringtoneKey = stringPreferencesKey("alarm_ringtone")

    val ringtoneFlow: Flow<AlarmSound> = context.dataStore.data.map { preferences ->
        val name = preferences[ringtoneKey] ?: AlarmSound.DEFAULT.name
        AlarmSound.fromName(name)
    }

    suspend fun saveRingtone(sound: AlarmSound) {
        context.dataStore.edit { preferences ->
            preferences[ringtoneKey] = sound.name
        }
    }

    fun getSelectedRingtoneSync(): AlarmSound {
        val prefs = context.getSharedPreferences("alarm_ringtone_cache", Context.MODE_PRIVATE)
        val name = prefs.getString("selected_ringtone", AlarmSound.DEFAULT.name) ?: AlarmSound.DEFAULT.name
        return AlarmSound.fromName(name)
    }

    suspend fun saveRingtoneWithCache(sound: AlarmSound) {
        // Save to DataStore (reactive)
        context.dataStore.edit { preferences ->
            preferences[ringtoneKey] = sound.name
        }
        // Save to SharedPreferences (for synchronous access in BroadcastReceiver)
        context.getSharedPreferences("alarm_ringtone_cache", Context.MODE_PRIVATE)
            .edit()
            .putString("selected_ringtone", sound.name)
            .apply()
    }
}
