package com.bananchiki.wakeup

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true
) {
    val timeFormatted: String
        get() = String.format("%02d:%02d", hour, minute)
}
