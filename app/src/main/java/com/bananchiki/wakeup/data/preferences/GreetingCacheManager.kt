package com.bananchiki.wakeup.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.greetingDataStore: DataStore<Preferences> by preferencesDataStore(name = "greetings_prefs")

val GREETINGS_KEY = stringSetPreferencesKey("my_greetings_set")

class GreetingCacheManager(private val context: Context) {
    fun greetingsFlow(): Flow<Set<String>> = context.greetingDataStore.data.map { preferences ->
        preferences[GREETINGS_KEY] ?: emptySet()
    }

    suspend fun addGreeting(newGreeting: String) {
        context.greetingDataStore.edit { preferences ->
            val currentSet = preferences[GREETINGS_KEY] ?: emptySet()

            if (currentSet.size >= 3){
                return@edit
            } else {
                preferences[GREETINGS_KEY] = currentSet + newGreeting
            }
        }
    }

    suspend fun removeGreeting(usedGreeting: String) {
        context.greetingDataStore.edit { preferences ->
            val currentSet = preferences[GREETINGS_KEY] ?: emptySet()
            preferences[GREETINGS_KEY] = currentSet - usedGreeting
        }
    }
}