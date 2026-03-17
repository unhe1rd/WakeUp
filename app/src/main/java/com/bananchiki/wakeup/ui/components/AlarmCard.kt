package com.bananchiki.wakeup.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananchiki.wakeup.data.model.Alarm
import com.bananchiki.wakeup.ui.theme.*

@Composable
fun AlarmCardGrid(
    alarms: List<Alarm>,
    onToggleAlarm: (Alarm, Boolean) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alarms.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "⏰", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No alarms yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = GrayMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap + to add your first alarm",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayLight
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
        ) {
            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { isEnabled -> onToggleAlarm(alarm, isEnabled) },
                    onDelete = { onDeleteAlarm(alarm) }
                )
            }
        }
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (alarm.isEnabled) AmberBorder else Color(0xFFE8E8E8),
        animationSpec = tween(300),
        label = "border"
    )
    val textAlpha = if (alarm.isEnabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top row: label + menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.isEnabled) DarkSubtext else GrayMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showMenu = true },
                        tint = GrayMedium
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = alarm.timeFormatted12,
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = DarkText.copy(alpha = textAlpha),
                        letterSpacing = (-1).sp
                    )
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = alarm.amPmLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = DarkText.copy(alpha = textAlpha)
                    ),
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days of week
            DaysOfWeekRow(alarm = alarm)

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: icon + toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (alarm.isEnabled) Amber else GrayLight
                )
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = ToggleOn,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = ToggleTrack,
                        uncheckedBorderColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DaysOfWeekRow(alarm: Alarm) {
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        dayLetters.forEachIndexed { index, letter ->
            val isActive = alarm.isDayActive(index)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(16.dp)
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = if (isActive) Amber else GrayLight,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                )
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Amber else Color.Transparent)
                )
            }
        }
    }
}
