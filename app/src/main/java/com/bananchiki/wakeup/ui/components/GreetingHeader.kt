package com.bananchiki.wakeup.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bananchiki.wakeup.data.model.Alarm
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun GreetingHeader(
    alarms: List<Alarm> = emptyList(),
    isPremium: Boolean = false,
    onProClick: () -> Unit = {}
) {
    val (greeting, emoji) = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> "Good Night" to "🌙"
            hour < 12 -> "Good Morning" to "☀️"
            hour < 18 -> "Good Afternoon" to "🌤️"
            else -> "Good Evening" to "🌆"
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = emoji, fontSize = 28.sp)
            }
            
            var timeTrigger by remember { mutableStateOf(System.currentTimeMillis()) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        timeTrigger = System.currentTimeMillis()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            LaunchedEffect(alarms) {
                while (true) {
                    delay(10_000) // Update more frequently to avoid stale 0m displays
                    timeTrigger = System.currentTimeMillis()
                }
            }
            
            val nextAlarmStr = getNextAlarmTimeStr(alarms, timeTrigger)
            if (nextAlarmStr != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = nextAlarmStr,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        // ProBadge(isPremium = isPremium, onClick = onProClick)
    }
}

fun getNextAlarmTimeStr(alarms: List<Alarm>, timeTrigger: Long = 0L): String? {
    val enabledAlarms = alarms.filter { it.isEnabled }
    if (enabledAlarms.isEmpty()) return null

    val now = Calendar.getInstance()
    var minTime = Long.MAX_VALUE

    for (alarm in enabledAlarms) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
        calendar.set(Calendar.MINUTE, alarm.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val activeDaysCount = alarm.daysOfWeek.count { it == '1' }
        var firingTime = -1L

        if (activeDaysCount == 0) {
            if (calendar.timeInMillis <= now.timeInMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            firingTime = calendar.timeInMillis
        } else {
            for (i in 0..7) {
                if (i > 0) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                if (i == 0 && calendar.timeInMillis <= now.timeInMillis) {
                    continue
                }
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val mappedIndex = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2
                
                if (alarm.isDayActive(mappedIndex)) {
                    firingTime = calendar.timeInMillis
                    break
                }
            }
        }
        
        if (firingTime != -1L && firingTime < minTime) {
            minTime = firingTime
        }
    }

    if (minTime == Long.MAX_VALUE) return null

    val diffMs = minTime - now.timeInMillis
    val diffMin = (diffMs / (1000 * 60)) % 60
    val diffHour = (diffMs / (1000 * 60 * 60))

    return if (diffHour > 0) {
        "Прозвонит через ${diffHour}ч ${diffMin}м"
    } else {
        "Прозвонит через ${diffMin}м"
    }
}
