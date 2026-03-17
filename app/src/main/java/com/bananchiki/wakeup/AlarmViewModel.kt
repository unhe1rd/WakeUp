package com.bananchiki.wakeup

import android.app.Application
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AlarmDatabase.getDatabase(application)
    private val alarmDao = db.alarmDao()

    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    fun addAlarm(hour: Int, minute: Int, label: String = "Wake up!", daysOfWeek: String = "0000000") {
        viewModelScope.launch {
            val alarm = Alarm(hour = hour, minute = minute, label = label, daysOfWeek = daysOfWeek)
            alarmDao.insert(alarm)
            // After insert, schedule it — we need the generated ID
            val allAlarmsList = alarmDao.getAllAlarmsList()
            val inserted = allAlarmsList.lastOrNull {
                it.hour == hour && it.minute == minute && it.label == label
            }
            inserted?.let { scheduleAlarm(it) }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            cancelAlarm(alarm)
            alarmDao.delete(alarm)
        }
    }

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = isEnabled)
            alarmDao.update(updated)
            if (isEnabled) {
                scheduleAlarm(updated)
            } else {
                cancelAlarm(updated)
            }
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmDao.update(alarm)
            if (alarm.isEnabled) {
                scheduleAlarm(alarm)
            } else {
                cancelAlarm(alarm)
            }
        }
    }

    private fun scheduleAlarm(alarm: Alarm) {
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val activeDays = alarm.daysOfWeek.mapIndexedNotNull { i, c ->
            if (c == '1') i else null
        }

        if (activeDays.isEmpty()) {
            // One-time alarm — schedule for next occurrence
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            setExactAlarm(context, alarmManager, alarm.id * 10, calendar.timeInMillis, alarm.id)
        } else {
            // Repeating for specific days
            for (dayIndex in activeDays) {
                // Convert our day index (0=Sun) to Calendar day (1=Sun)
                val calendarDay = dayIndex + 1
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_WEEK, calendarDay)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }
                val requestCode = alarm.id * 10 + dayIndex
                setExactAlarm(context, alarmManager, requestCode, calendar.timeInMillis, alarm.id)
            }
        }
    }

    private fun setExactAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int, triggerAtMillis: Long, alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(alarm: Alarm) {
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel all possible pending intents for this alarm (one-time + 7 days)
        for (dayIndex in 0..9) {
            val requestCode = alarm.id * 10 + dayIndex
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
