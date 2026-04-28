package com.bananchiki.wakeup.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object AchievementManager {
    private const val PREFS_NAME = "achievements"
    private const val KEY_TOTAL_WAKEUPS = "total_wakeups"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_BEST_STREAK = "best_streak"
    private const val KEY_LAST_DATE = "last_date"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun isYesterday(dateStr: String?): Boolean {
        if (dateStr == null) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val today = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(cal.time)
        return dateStr == yesterday
    }

    fun registerWakeUp(context: Context) {
        val prefs = getPrefs(context)
        val today = getCurrentDateString()

        val total = prefs.getInt(KEY_TOTAL_WAKEUPS, 0) + 1
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val bestStreak = prefs.getInt(KEY_BEST_STREAK, 0)

        val newStreak = when {
            lastDate == null -> 1
            lastDate == today -> currentStreak
            isYesterday(lastDate) -> currentStreak + 1
            else -> 1
        }

        prefs.edit()
            .putInt(KEY_TOTAL_WAKEUPS, total)
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .putInt(KEY_BEST_STREAK, maxOf(bestStreak, newStreak))
            .apply()
    }

    fun getTotalWakeUps(context: Context): Int = getPrefs(context).getInt(KEY_TOTAL_WAKEUPS, 0)
    fun getCurrentStreak(context: Context): Int = getPrefs(context).getInt(KEY_CURRENT_STREAK, 0)
    fun getBestStreak(context: Context): Int = getPrefs(context).getInt(KEY_BEST_STREAK, 0)

    fun getUserLevel(context: Context): Pair<Int, String> {
        val total = getTotalWakeUps(context)
        return when {
            total < 5 -> 1 to "Соня"
            total < 15 -> 2 to "Просыпающийся"
            total < 40 -> 3 to "Жаворонок-стажёр"
            total < 80 -> 4 to "Уверенный будильник"
            total < 250 -> 5 to "Ранняя пташка"
            else -> 10 to "Хранитель режима"
        }
    }

    fun getProgressToNextLevel(context: Context): Pair<Int, Int> {
        val total = getTotalWakeUps(context)
        return when {
            total < 5 -> total to 5
            total < 15 -> total to 15
            total < 40 -> total to 40
            total < 80 -> total to 80
            total < 250 -> total to 250
            else -> total to total
        }
    }
}