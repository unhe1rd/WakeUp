package com.bananchiki.wakeup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.data.model.Alarm
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.viewinterop.AndroidView
import com.bananchiki.wakeup.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    alarmToEdit: Alarm? = null,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String, String, String) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(alarmToEdit?.hour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(alarmToEdit?.minute ?: Calendar.getInstance().get(Calendar.MINUTE)) }
    var label by remember { mutableStateOf(alarmToEdit?.label ?: "Просыпайся!") }
    var daysOfWeek by remember { mutableStateOf(alarmToEdit?.daysOfWeek ?: "0000000") }
    var selectedTaskType by remember { mutableStateOf(alarmToEdit?.taskType ?: "NONE") }
    
    val dayLetters = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (alarmToEdit != null) "Редактирование" else "Новый будильник", 
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Time Picker Spinner inline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    key(isDarkTheme) {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth(),
                            factory = { context ->
                                val themeRes = if (isDarkTheme) R.style.SpinnerTimePickerDark else R.style.SpinnerTimePickerLight
                                val themedContext = android.view.ContextThemeWrapper(context, themeRes)
                                val view = android.view.LayoutInflater.from(themedContext).inflate(R.layout.time_picker_spinner, null) as android.widget.TimePicker
                                view.setIs24HourView(true)
                                view.hour = selectedHour
                                view.minute = selectedMinute
                                view.setOnTimeChangedListener { _, h, m ->
                                    selectedHour = h
                                    selectedMinute = m
                                }
                                view
                            },
                            update = { view ->
                                if (view.hour != selectedHour) view.hour = selectedHour
                                if (view.minute != selectedMinute) view.minute = selectedMinute
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Days of week selector
                Text(
                    "Повтор",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
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
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Task Type Selector
                Text(
                    "Задание для отключения",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val tasks: List<Triple<String, String, ImageVector>> = listOf(
                    Triple("NONE", " Обычное нажатие", Icons.Default.Check),
                    Triple("MATH", " Решить пример", Icons.Default.Add),
                    Triple("MEMORY", " Найти пары", Icons.Default.Apps),
                    Triple("AI", " Ввод текста", Icons.Default.AutoAwesome),
                    Triple("REWARDED", " Посмотреть рекламу \uD83E\uDEF6", Icons.Default.PlayArrow)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.forEach { (type, title, icon) ->
                        val isSelected = selectedTaskType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { selectedTaskType = type }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour, selectedMinute, label, daysOfWeek, selectedTaskType) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
