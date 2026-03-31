package com.bananchiki.wakeup.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.data.preferences.ThemeSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeSettings,
    onThemeSelected: (ThemeSettings) -> Unit
) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
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
                onClick = { onThemeSelected(ThemeSettings.LIGHT) }
            )
            ThemeSelectionRow(
                text = "Тёмная",
                selected = currentTheme == ThemeSettings.DARK,
                onClick = { onThemeSelected(ThemeSettings.DARK) }
            )
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
