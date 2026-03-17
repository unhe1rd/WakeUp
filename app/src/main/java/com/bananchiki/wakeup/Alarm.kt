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
    val isEnabled: Boolean = true,
    val daysOfWeek: String = "0000000" // S M T W T F S — "1" = active, "0" = inactive
) {
    val timeFormatted: String
        get() = String.format("%02d:%02d", hour, minute)

    val hour12: Int
        get() {
            val h = hour % 12
            return if (h == 0) 12 else h
        }

    val amPmLabel: String
        get() = if (hour < 12) "AM" else "PM"

    val timeFormatted12: String
        get() = String.format("%d:%02d", hour12, minute)

    val daysLabel: String
        get() {
            val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
            val active = daysOfWeek.mapIndexedNotNull { i, c ->
                if (c == '1') dayNames[i] else null
            }
            return when {
                active.size == 7 -> "Everyday"
                active.isEmpty() -> "Once"
                else -> active.joinToString(" ")
            }
        }

    fun isDayActive(dayIndex: Int): Boolean {
        return dayIndex in daysOfWeek.indices && daysOfWeek[dayIndex] == '1'
    }
}
