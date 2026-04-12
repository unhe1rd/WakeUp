package com.bananchiki.wakeup.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.statsDataStore by preferencesDataStore(name = "stats_prefs")

class StatsPreferenceManager(private val context: Context) {

    companion object {
        val AVG_WAKE_UP_TIME = stringPreferencesKey("avg_wake_up_time")
        val SNOOZE_COUNT = intPreferencesKey("snooze_count")
        val AVG_TASK_TIME_SEC = intPreferencesKey("avg_task_time_sec")
        val MOOD_GREAT_COUNT = intPreferencesKey("mood_great_count")
        val MOOD_OKAY_COUNT = intPreferencesKey("mood_okay_count")
        val MOOD_TIRED_COUNT = intPreferencesKey("mood_tired_count")
    }

    val avgWakeUpTimeFlow: Flow<String?> = context.statsDataStore.data.map { it[AVG_WAKE_UP_TIME] }
    val snoozeCountFlow: Flow<Int> = context.statsDataStore.data.map { it[SNOOZE_COUNT] ?: 0 }
    val avgTaskTimeFlow: Flow<Int?> = context.statsDataStore.data.map { it[AVG_TASK_TIME_SEC] }
    val moodGreatFlow: Flow<Int> = context.statsDataStore.data.map { it[MOOD_GREAT_COUNT] ?: 0 }
    val moodOkayFlow: Flow<Int> = context.statsDataStore.data.map { it[MOOD_OKAY_COUNT] ?: 0 }
    val moodTiredFlow: Flow<Int> = context.statsDataStore.data.map { it[MOOD_TIRED_COUNT] ?: 0 }

    suspend fun saveAvgWakeUpTime(time: String) {
        context.statsDataStore.edit { it[AVG_WAKE_UP_TIME] = time }
    }

    suspend fun saveSnoozeCount(count: Int) {
        context.statsDataStore.edit { it[SNOOZE_COUNT] = count }
    }

    suspend fun saveAvgTaskTime(sec: Int) {
        context.statsDataStore.edit { it[AVG_TASK_TIME_SEC] = sec }
    }
    
    suspend fun saveMoodCount(great: Int, okay: Int, tired: Int) {
        context.statsDataStore.edit { prefs ->
            prefs[MOOD_GREAT_COUNT] = great
            prefs[MOOD_OKAY_COUNT] = okay
            prefs[MOOD_TIRED_COUNT] = tired
        }
    }
}
