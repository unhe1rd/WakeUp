package com.bananchiki.wakeup.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Amber,
    onPrimary = DarkText,
    primaryContainer = AmberSurface,
    onPrimaryContainer = DarkText,
    secondary = AmberLight,
    onSecondary = DarkText,
    secondaryContainer = AmberSurface,
    onSecondaryContainer = DarkText,
    tertiary = AmberDark,
    onTertiary = White,
    background = BackgroundLight,
    onBackground = DarkText,
    surface = White,
    onSurface = DarkText,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = GrayText,
    outline = GrayLight,
    outlineVariant = AmberBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4A574),
    onPrimary = Color(0xFF2C1810),

    primaryContainer = Color(0xFF4A3020),
    onPrimaryContainer = Color(0xFFE8CDB5),
    secondary = Color(0xFFC49A6C),
    onSecondary = Color(0xFF2C1810),
    secondaryContainer = Color(0xFF5D4030),
    onSecondaryContainer = Color(0xFFD4B89A),
    tertiary = Color(0xFFB8865A),
    onTertiary = Color(0xFF2C1810),
    tertiaryContainer = Color(0xFF6B4A35),
    onTertiaryContainer = Color(0xFFC9A889),
    background = Color(0xFF1A1410),
    onBackground = Color(0xFFEDE0D4),
    surface = Color(0xFF241C16),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF2E241C),
    onSurfaceVariant = Color(0xFFC4A891),
    outline = Color(0xFF6B5242),
    outlineVariant = Color(0xFF3D2B20),
    error = Color(0xFFD47A7A),
    onError = Color(0xFF2C1810),
    errorContainer = Color(0xFF4A2A2A),
    onErrorContainer = Color(0xFFE8C5C5)
)

@Composable
fun WakeUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
