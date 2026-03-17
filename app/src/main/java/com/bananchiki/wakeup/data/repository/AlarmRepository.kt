package com.bananchiki.wakeup.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bananchiki.wakeup.data.local.AlarmDao
import com.bananchiki.wakeup.data.model.Alarm
import com.bananchiki.wakeup.receiver.AlarmReceiver
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val context: Context
) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun insert(alarm: Alarm) {
        alarmDao.insert(alarm)
    }

    suspend fun update(alarm: Alarm) {
        alarmDao.update(alarm)
    }

    suspend fun delete(alarm: Alarm) {
        alarmDao.delete(alarm)
    }

    suspend fun getAllAlarmsList(): List<Alarm> {
        return alarmDao.getAllAlarmsList()
    }

    // ==================== AlarmManager Scheduling ====================

    fun scheduleAlarm(alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val activeDays = alarm.daysOfWeek.mapIndexedNotNull { i, c ->
            if (c == '1') i else null
        }

        if (activeDays.isEmpty()) {
            // One-time alarm
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            setExactAlarm(alarmManager, alarm.id * 10, calendar.timeInMillis, alarm.id)
        } else {
            // Repeating for specific days
            for (dayIndex in activeDays) {
                val calendarDay = dayIndex + 1 // Our index (0=Sun) → Calendar (1=Sun)
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
                setExactAlarm(alarmManager, requestCode, calendar.timeInMillis, alarm.id)
            }
        }
    }

    fun cancelAlarm(alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

    private fun setExactAlarm(
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerAtMillis: Long,
        alarmId: Int
    ) {
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
}
