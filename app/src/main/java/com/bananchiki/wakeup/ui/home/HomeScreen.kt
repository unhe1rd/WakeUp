package com.bananchiki.wakeup.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.data.model.Alarm
import com.bananchiki.wakeup.ui.components.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.bananchiki.wakeup.ui.goals.AchievementsScreen
import com.bananchiki.wakeup.ui.theme.Amber

@Composable
fun HomeScreen(
    alarms: List<Alarm>,
    onDeleteAlarm: (Alarm) -> Unit,
    onToggleAlarm: (Alarm, Boolean) -> Unit,
    onEditAlarm: (Alarm) -> Unit,
    isPremium: Boolean = false,
    onProClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GreetingHeader(
            isPremium = isPremium,
            onProClick = onProClick
        )
        WeekCalendarStrip()

        Spacer(modifier = Modifier.height(16.dp))

        // Separation line
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Alarms list
        AlarmCardGrid(
            alarms = alarms,
            onToggleAlarm = onToggleAlarm,
            onDeleteAlarm = onDeleteAlarm,
            onEditAlarm = onEditAlarm,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
    }
}
