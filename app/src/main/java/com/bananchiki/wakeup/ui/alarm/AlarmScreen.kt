package com.bananchiki.wakeup.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananchiki.wakeup.ui.theme.Amber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AlarmScreen(onDismiss: () -> Unit, label: String = "Wake up!") {
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance().time
        }
    }

    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE,  dd MMM", Locale.getDefault())

    val timeString = timeFormat.format(currentTime)
    val dateString = dateFormat.format(currentTime)

    // Beautiful soft gradient matching the design
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF8C7A), // Soft coral / peach
            Color(0xFFD889E6), // Light purple / magenta
            Color(0xFFE5F0FF), // Pale light blue
            Color(0xFFFFFFFF)  // White
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Weather Icon (using emoji here for simplicity)
            Text(
                text = "⛅",
                fontSize = 72.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date
            Text(
                text = dateString,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large Time
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 110.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Alarm Label
            Text(
                text = label,
                color = Color(0xFF333333),
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Snooze Button
            Button(
                onClick = { /* Snooze could just dismiss for now */ onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.25f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Text(
                    text = "Snooze",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Dismiss Button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor = Color(0xFF1A1A1A) // Dark text on amber
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    text = "Dismiss",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun Cards(number: Int, isError: Boolean = false, onCardClick: () -> Unit){
    Card(
        onClick = { onCardClick() },
        shape = RoundedCornerShape(40.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if(!isError){
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)

    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .size(30.dp)
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

@Preview
@Composable
fun Preview(){
    AlarmScreen({})
}
