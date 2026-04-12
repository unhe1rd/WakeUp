package com.bananchiki.wakeup.ui.alarm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananchiki.wakeup.data.tasks.generateMathTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MathTaskScreen(onComplete: () -> Unit) {
    var currentTask by remember { mutableStateOf(generateMathTask()) }
    var solvedCount by remember { mutableIntStateOf(0) }
    val totalRequired = 3
    val scope = rememberCoroutineScope()
    var wrongClickedNumber by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Решите $totalRequired задачки: ${solvedCount + 1}/$totalRequired",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = currentTask.question,
            color = Color.White,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        val chunkedCards = currentTask.cards.chunked(3)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (rowArgs in chunkedCards) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (number in rowArgs) {
                        val isError = wrongClickedNumber == number
                        Card(
                            onClick = {
                                if (wrongClickedNumber != null) return@Card
                                if (number == currentTask.answer) {
                                    solvedCount++
                                    if (solvedCount >= totalRequired) {
                                        onComplete()
                                    } else {
                                        currentTask = generateMathTask()
                                    }
                                } else {
                                    scope.launch {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        wrongClickedNumber = number
                                        delay(700)
                                        wrongClickedNumber = null
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (!isError) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.error,
                                contentColor = if (!isError) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onError
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isError) 0.dp else 4.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = number.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
