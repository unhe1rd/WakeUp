package com.bananchiki.wakeup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekCalendarStrip() {
    val calendar = remember { Calendar.getInstance() }
    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val currentMonth = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    val weekDays = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        (0..6).map { offset ->
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, offset)
            Triple(
                SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time).take(3),
                dayCal.get(Calendar.DAY_OF_MONTH),
                dayCal.get(Calendar.DAY_OF_YEAR) == today
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { (dayName, date, isToday) ->
                DayChip(dayName = dayName, date = date, isToday = isToday)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentMonth,
            style = MaterialTheme.typography.titleMedium.copy(
                color = Amber,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DayChip(dayName: String, date: Int, isToday: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(44.dp)
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) DarkText else GrayMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isToday) Amber else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isToday) White else GrayMedium
            )
        }
    }
}
