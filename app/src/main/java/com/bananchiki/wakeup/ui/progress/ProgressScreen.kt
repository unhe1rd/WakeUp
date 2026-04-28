package com.bananchiki.wakeup.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananchiki.wakeup.data.preferences.StatsPreferenceManager
import com.bananchiki.wakeup.ui.theme.Amber
import com.bananchiki.wakeup.ui.theme.DarkText
import com.bananchiki.wakeup.ui.theme.GrayMedium

@Composable
fun ProgressScreen(
    isPremium: Boolean = false,
    onProClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val statsPrefs = remember { StatsPreferenceManager(context) }

    val avgWakeUpTime by statsPrefs.avgWakeUpTimeFlow.collectAsState(initial = null)
    val snoozeCount by statsPrefs.snoozeCountFlow.collectAsState(initial = 0)
    val avgTaskTimeSec by statsPrefs.avgTaskTimeFlow.collectAsState(initial = null)
    val moodGreat by statsPrefs.moodGreatFlow.collectAsState(initial = 0)
    val moodOkay by statsPrefs.moodOkayFlow.collectAsState(initial = 0)
    val moodTired by statsPrefs.moodTiredFlow.collectAsState(initial = 0)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Track your gamified wake-up stats",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. Average Wake-Up Time Card (always visible)
            WakeUpTimeCard(avgWakeUpTime)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Wake-Up Performance — blurred for free users
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = if (!isPremium) Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .blur(8.dp)
                    else Modifier
                ) {
                    WakeUpStatsCard(snoozeCount, avgTaskTimeSec)
                    Spacer(modifier = Modifier.height(16.dp))
                    MoodStatsCard(moodGreat, moodOkay, moodTired)
                }

                // Lock overlay for free users
                if (!isPremium) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onProClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Amber,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Полная статистика",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Доступна в WakeUp Pro",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onProClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Amber,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Попробовать Pro", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Extra padding for bottom navbar
        }
    }
}

@Composable
fun WakeUpTimeCard(avgWakeUpTime: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Average Wake-Up Time", style = MaterialTheme.typography.labelMedium, color = GrayMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = avgWakeUpTime ?: "--:--", 
                fontSize = 32.sp, 
                fontWeight = FontWeight.Bold, 
                color = DarkText
            )
        }
    }
}

@Composable
fun WakeUpStatsCard(snoozeCount: Int, avgTaskTimeSec: Int?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Wake-Up Performance", style = MaterialTheme.typography.labelLarge, color = GrayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Snoozes hit", fontSize = 16.sp, color = DarkText, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(if (snoozeCount == 0) "Perfect!" else "Try to reduce this", fontSize = 12.sp, color = GrayMedium)
                }
                Box(
                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.errorContainer.copy(alpha=0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$snoozeCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Avg. Task Time", fontSize = 16.sp, color = DarkText, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Time to clear alarms", fontSize = 12.sp, color = GrayMedium)
                }
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .background(Amber.copy(alpha=0.2f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (avgTaskTimeSec != null) "${avgTaskTimeSec}s" else "--", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Amber
                    )
                }
            }
        }
    }
}

@Composable
fun MoodStatsCard(great: Int, okay: Int, tired: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Morning Mood", style = MaterialTheme.typography.labelLarge, color = GrayMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MoodItem(emoji = "🤩", title = "Great", count = great)
                MoodItem(emoji = "😐", title = "Okay", count = okay)
                MoodItem(emoji = "😴", title = "Tired", count = tired)
            }
        }
    }
}

@Composable
fun MoodItem(emoji: String, title: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, color = GrayMedium, fontWeight = FontWeight.Medium)
        Text(if (count > 0) "$count" else "--", fontSize = 18.sp, color = DarkText, fontWeight = FontWeight.Bold)
    }
}
