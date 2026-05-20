package com.bananchiki.wakeup.ui.settings

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import com.bananchiki.wakeup.data.model.AlarmSound
import com.bananchiki.wakeup.data.model.AlarmSoundCategory
import com.bananchiki.wakeup.data.preferences.RingtonePreferenceManager
import com.bananchiki.wakeup.data.preferences.ThemeSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeSettings,
    onThemeSelected: (ThemeSettings) -> Unit,
    isPremium: Boolean = false,
    onProClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val ringtoneManager = remember { RingtonePreferenceManager(context) }
    val selectedRingtone by ringtoneManager.ringtoneFlow.collectAsState(initial = AlarmSound.DEFAULT)
    val scope = rememberCoroutineScope()
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Clean up preview player when leaving
    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.release()
        }
    }

    fun previewSound(sound: AlarmSound) {
        previewPlayer?.release()
        previewPlayer = null
        try {
            previewPlayer = MediaPlayer.create(context, sound.rawResId)?.apply {
                start()
            }
            // Stop after 5 seconds
            scope.launch {
                delay(5000)
                previewPlayer?.release()
                previewPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        val uriHandler = LocalUriHandler.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // WakeUp Pro card
            if (!isPremium) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFC107),
                                        Color(0xFFFF9800)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WakeUp Pro",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Разблокируй все функции",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "→",
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // === Alarm Sound Section ===
            Text(
                text = "Выбрать рингтон",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AlarmSoundCategory.entries.forEach { category ->
                var expanded by remember { mutableStateOf(false) }
                
                // Category header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        AlarmSound.entries
                            .filter { it.category == category }
                            .forEach { sound ->
                                RingtoneSelectionRow(
                                    text = sound.displayName,
                                    selected = selectedRingtone == sound,
                                    onClick = {
                                        scope.launch {
                                            ringtoneManager.saveRingtoneWithCache(sound)
                                        }
                                        previewSound(sound)
                                    }
                                )
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === Theme Section ===
            Text(
                text = "Внешний вид",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ThemeSelectionRow(
                text = "Системная",
                selected = currentTheme == ThemeSettings.SYSTEM,
                onClick = { onThemeSelected(ThemeSettings.SYSTEM) }
            )
            ThemeSelectionRow(
                text = "Светлая",
                selected = currentTheme == ThemeSettings.LIGHT,
                onClick = {
                    if (isPremium) {
                        onThemeSelected(ThemeSettings.LIGHT)
                    } else {
                        onProClick()
                    }
                },
                locked = !isPremium
            )
            ThemeSelectionRow(
                text = "Тёмная",
                selected = currentTheme == ThemeSettings.DARK,
                onClick = {
                    if (isPremium) {
                        onThemeSelected(ThemeSettings.DARK)
                    } else {
                        onProClick()
                    }
                },
                locked = !isPremium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "О приложении",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            DocumentLinkRow(
                text = "Privacy Policy",
                onClick = { uriHandler.openUri("https://www.notion.so/Privacy-Policy-3651fb3759448035b0c2d18f03120a26?source=copy_link") }
            )
            DocumentLinkRow(
                text = "Terms & Conditions",
                onClick = { uriHandler.openUri("https://www.notion.so/Terms-Conditions-3651fb37594480b3b4daf989c4c4ef94?source=copy_link") }
        )
        }
    }
}

@Composable
private fun RingtoneSelectionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Text(
                text = "▶",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    locked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (locked) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Pro only",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DocumentLinkRow(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "↗",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
