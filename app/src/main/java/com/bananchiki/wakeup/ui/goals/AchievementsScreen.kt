package com.bananchiki.wakeup.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananchiki.wakeup.data.AchievementManager
import com.bananchiki.wakeup.ui.theme.*

@Composable
fun AchievementsScreen(
    isPremium: Boolean = false,
    onProClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val totalWakeUps = remember { mutableStateOf(AchievementManager.getTotalWakeUps(context)) }
    val currentStreak = remember { mutableStateOf(AchievementManager.getCurrentStreak(context)) }
    val bestStreak = remember { mutableStateOf(AchievementManager.getBestStreak(context)) }
    val (level, levelTitle) = AchievementManager.getUserLevel(context)
    val (progress, maxProgress) = AchievementManager.getProgressToNextLevel(context)

    LaunchedEffect(Unit) {
        totalWakeUps.value = AchievementManager.getTotalWakeUps(context)
        currentStreak.value = AchievementManager.getCurrentStreak(context)
        bestStreak.value = AchievementManager.getBestStreak(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Уровень $level", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    Text(levelTitle, fontSize = 18.sp, color = GrayMedium, modifier = Modifier.padding(bottom = 12.dp))
                    LinearProgressIndicator(
                        progress = progress.toFloat() / maxProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4A90E2),
                        trackColor = GrayMedium.copy(alpha = 0.2f)
                    )
                    Text("$progress / $maxProgress подъёмов", fontSize = 14.sp, color = GrayMedium, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StreakCard("🔥 Текущая серия", currentStreak.value, Modifier.weight(1f))
                StreakCard("🏆 Рекорд", bestStreak.value, Modifier.weight(1f))
            }
        }

        item {
            Text("Награды за дисциплину", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = DarkText, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        }

        items(achievementsList) { achievement ->
            AchievementItem(
                title = achievement.title,
                description = achievement.description,
                current = when (achievement.id) {
                    "streak_3" -> currentStreak.value
                    "streak_7" -> currentStreak.value
                    "streak_14" -> currentStreak.value
                    "streak_30" -> currentStreak.value
                    "total_50" -> totalWakeUps.value
                    "total_100" -> totalWakeUps.value
                    "pro_supporter" -> if (isPremium) 1 else 0
                    else -> 0
                },
                required = achievement.requiredCount
            )
        }


    }
}

@Composable
fun StreakCard(title: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 14.sp, color = GrayMedium)
            Text(text = value.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.padding(top = 4.dp))
            Text(text = "дней", fontSize = 12.sp, color = GrayMedium)
        }
    }
}

@Composable
fun AchievementItem(title: String, description: String, current: Int, required: Int) {
    val completed = current >= required
    val progressVal = (current.toFloat() / required).coerceIn(0f, 1f)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (completed) Color(0xFFE8F5E9) else Color(0xFFF9F9F9))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                Text(description, fontSize = 13.sp, color = GrayMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = progressVal, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = if (completed) Color(0xFF4CAF50) else Color(0xFF4A90E2), trackColor = GrayMedium.copy(alpha = 0.2f))
                Text("$current / $required", fontSize = 12.sp, color = GrayMedium, modifier = Modifier.padding(top = 4.dp))
            }
            if (completed) { Text("✅", fontSize = 24.sp, modifier = Modifier.padding(start = 8.dp)) }
        }
    }
}

private val achievementsList = listOf(
    AchievementData("pro_supporter", "💎 Спонсор разработки", "Поддержи приложение подпиской", 1),
    AchievementData("streak_3", "Начало положено", "Просыпаться 3 дня подряд", 3),
    AchievementData("streak_7", "Входим во вкус", "Просыпаться 7 дней подряд", 7),
    AchievementData("streak_14", "Дисциплина", "Просыпаться 14 дней подряд", 14),
    AchievementData("streak_30", "Месяц без срывов", "Просыпаться 30 дней подряд", 30),
    AchievementData("total_50", "Полтинник", "Всего 50 подъёмов", 50),
    AchievementData("total_100", "Сотня", "Всего 100 подъёмов", 100)
)

data class AchievementData(val id: String, val title: String, val description: String, val requiredCount: Int)
