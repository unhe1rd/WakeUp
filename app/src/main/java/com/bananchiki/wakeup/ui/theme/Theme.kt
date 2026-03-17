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
    primary = AmberLight,
    onPrimary = DarkText,
    primaryContainer = Color(0xFF3E2723),
    onPrimaryContainer = AmberLight,
    secondary = Amber,
    onSecondary = DarkText,
    background = Color(0xFF121212),
    onBackground = White,
    surface = Color(0xFF1E1E1E),
    onSurface = White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = GrayLight,
    outline = GrayMedium,
    outlineVariant = AmberDark
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