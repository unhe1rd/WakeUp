package com.bananchiki.wakeup.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.data.model.Alarm
import com.bananchiki.wakeup.ui.components.*
import com.bananchiki.wakeup.ui.theme.BackgroundLight

@Composable
fun HomeScreen(
    alarms: List<Alarm>,
    onAddAlarm: (Int, Int, String, String) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onToggleAlarm: (Alarm, Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            BottomNavBar(onAddClick = { showAddDialog = true })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GreetingHeader()
            Spacer(modifier = Modifier.height(8.dp))
            WeekCalendarStrip()
            Spacer(modifier = Modifier.height(16.dp))
            AlarmGoalsTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AlarmCardGrid(
                alarms = alarms,
                onToggleAlarm = onToggleAlarm,
                onDeleteAlarm = onDeleteAlarm,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { hour, minute, label, days ->
                onAddAlarm(hour, minute, label, days)
                showAddDialog = false
            }
        )
    }
}
