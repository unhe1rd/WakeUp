package com.bananchiki.wakeup.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.ui.theme.*
import java.util.Calendar

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
            Text("New Alarm", style = MaterialTheme.typography.headlineMedium)
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
