package com.bananchiki.wakeup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AlarmDatabase.getDatabase(application)
    private val alarmDao = db.alarmDao()

    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    fun addAlarm(hour: Int, minute: Int) {
        viewModelScope.launch {
            val alarm = Alarm(hour = hour, minute = minute, label = "Wake Up!")
            alarmDao.insert(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmDao.delete(alarm)
        }
    }
    
    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            alarmDao.update(alarm.copy(isEnabled = isEnabled))
        }
    }
}
