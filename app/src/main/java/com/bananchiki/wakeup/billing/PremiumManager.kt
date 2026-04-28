package com.bananchiki.wakeup.billing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "premium_settings")

class PremiumManager(private val context: Context) {

    companion object {
        private val IS_PREMIUM_KEY = booleanPreferencesKey("is_premium")
        const val FREE_ALARM_LIMIT = 3
    }

    val isPremiumFlow: Flow<Boolean> = context.premiumDataStore.data.map { preferences ->
        preferences[IS_PREMIUM_KEY] ?: false
    }

    suspend fun updatePremiumStatus(isPremium: Boolean) {
        context.premiumDataStore.edit { preferences ->
            preferences[IS_PREMIUM_KEY] = isPremium
        }
    }

    /** Free: max 3 alarms */
    fun canCreateAlarm(currentCount: Int, isPremium: Boolean): Boolean {
        return isPremium || currentCount < FREE_ALARM_LIMIT
    }

    /** Free: only MATH task */
    fun canUseTask(taskType: String, isPremium: Boolean): Boolean {
        if (isPremium) return true
        return taskType == "NONE" || taskType == "MATH"
    }

    /** Free: only SYSTEM theme */
    fun canChangeTheme(isPremium: Boolean): Boolean {
        return isPremium
    }
}
