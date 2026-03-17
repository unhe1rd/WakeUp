package com.bananchiki.wakeup

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bananchiki.wakeup.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val alarmViewModel: AlarmViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permission denied. Alarm won't show!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestNotificationsPermission()

        setContent {
            WakeUpTheme {
                val alarms by alarmViewModel.allAlarms.collectAsState(initial = emptyList())
                WakeUpMainScreen(
                    alarms = alarms,
                    onAddAlarm = { hour, minute, label, daysOfWeek ->
                        alarmViewModel.addAlarm(hour, minute, label, daysOfWeek)
                    },
                    onDeleteAlarm = { alarm -> alarmViewModel.deleteAlarm(alarm) },
                    onToggleAlarm = { alarm, isEnabled -> alarmViewModel.toggleAlarm(alarm, isEnabled) }
                )
            }
        }
    }

    private fun checkAndRequestNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

// ===================== MAIN SCREEN =====================

@Composable
fun WakeUpMainScreen(
    alarms: List<Alarm>,
    onAddAlarm: (Int, Int, String, String) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onToggleAlarm: (Alarm, Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            BottomNavBar(
                onAddClick = { showAddDialog = true }
            )
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

// ===================== GREETING HEADER =====================

@Composable
fun GreetingHeader() {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> "Good Night"
            hour < 12 -> "Good Morning"
            hour < 18 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "☀️", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Mikhail Ulanov",
            style = MaterialTheme.typography.headlineLarge,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Let's build your habits",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText
        )
    }
}

// ===================== WEEK CALENDAR STRIP =====================

@Composable
fun WeekCalendarStrip() {
    val calendar = remember { Calendar.getInstance() }
    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val currentMonth = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    // Generate days for the current week
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
        // Day strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { (dayName, date, isToday) ->
                DayChip(dayName = dayName, date = date, isToday = isToday)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Month name
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
fun DayChip(dayName: String, date: Int, isToday: Boolean) {
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

// ===================== TABS =====================

@Composable
fun AlarmGoalsTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Alarm", "Goals")

    Row(
        modifier = Modifier
            .padding(horizontal = 60.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF0F0F0))
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedTab
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) White else Color.Transparent,
                animationSpec = tween(200),
                label = "tabBg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) DarkText else GrayMedium
                )
            }
        }
    }
}

// ===================== ALARM CARD GRID =====================

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

// ===================== ALARM CARD =====================

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
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
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
fun DaysOfWeekRow(alarm: Alarm) {
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
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

// ===================== ADD ALARM DIALOG =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String, String) -> Unit
) {
    val context = LocalContext.current
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var label by remember { mutableStateOf("Wake up!") }
    var daysOfWeek by remember { mutableStateOf("0000000") }
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = {
            Text(
                "New Alarm",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column {
                // Time display — tap to change
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberSurface)
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    selectedHour = h
                                    selectedMinute = m
                                },
                                selectedHour,
                                selectedMinute,
                                true
                            ).show()
                        }
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.displayLarge,
                        color = Amber
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        cursorColor = Amber,
                        focusedLabelColor = Amber
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Days of week selector
                Text(
                    "Repeat",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkSubtext
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dayLetters.forEachIndexed { index, letter ->
                        val isSelected = daysOfWeek.getOrElse(index) { '0' } == '1'
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Amber else Color(0xFFF0F0F0))
                                .clickable {
                                    val chars = daysOfWeek.toCharArray()
                                    chars[index] = if (chars[index] == '1') '0' else '1'
                                    daysOfWeek = String(chars)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) White else GrayMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour, selectedMinute, label, daysOfWeek) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor = White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GrayText)
            }
        }
    )
}

// ===================== BOTTOM NAV BAR =====================

@Composable
fun BottomNavBar(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // Background bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter),
            color = White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }
                ) {
                    Icon(Icons.Filled.Home, contentDescription = "Home", tint = Amber, modifier = Modifier.size(24.dp))
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(width = 16.dp, height = 3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Amber)
                    )
                }

                // Mail
                Icon(
                    Icons.Outlined.MailOutline,
                    contentDescription = "Messages",
                    tint = NavIconInactive,
                    modifier = Modifier.size(24.dp)
                )

                // Spacer for FAB
                Spacer(modifier = Modifier.width(56.dp))

                // Clock
                Icon(
                    Icons.Outlined.Alarm,
                    contentDescription = "Clock",
                    tint = NavIconInactive,
                    modifier = Modifier.size(24.dp)
                )

                // Settings
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = NavIconInactive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // FAB in center
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(56.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            containerColor = Amber,
            contentColor = White
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Alarm",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
