package com.bananchiki.wakeup.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bananchiki.wakeup.data.local.AlarmDatabase
import com.bananchiki.wakeup.data.model.Alarm
import com.bananchiki.wakeup.data.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository

    val allAlarms: Flow<List<Alarm>>

    init {
        val db = AlarmDatabase.getDatabase(application)
        repository = AlarmRepository(db.alarmDao(), application)
        allAlarms = repository.allAlarms
    }

    fun addAlarm(hour: Int, minute: Int, label: String = "Wake up!", daysOfWeek: String = "0000000") {
        viewModelScope.launch {
            val alarm = Alarm(hour = hour, minute = minute, label = label, daysOfWeek = daysOfWeek)
            repository.insert(alarm)
            // Schedule after insert — fetch inserted alarm to get generated ID
            val allAlarmsList = repository.getAllAlarmsList()
            val inserted = allAlarmsList.lastOrNull {
                it.hour == hour && it.minute == minute && it.label == label
            }
            inserted?.let { repository.scheduleAlarm(it) }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.cancelAlarm(alarm)
            repository.delete(alarm)
        }
    }

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = isEnabled)
            repository.update(updated)
            if (isEnabled) {
                repository.scheduleAlarm(updated)
            } else {
                repository.cancelAlarm(updated)
            }
        }
    }
}
