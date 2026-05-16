package com.bananchiki.wakeup.ui.onboarding

import android.Manifest
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onSaveAndFinishOnboarding: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isSuccess ->
        if (!isSuccess) {
            Toast.makeText(context, "Permission denied. Alarm won't show!", Toast.LENGTH_LONG).show()
        } else {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    val fullScreenIntentPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            if (notificationManager.canUseFullScreenIntent()){
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        } else {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
        data = Uri.parse("package:${context.packageName}")
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Уведомления",
                    subtitle = "Для правильной работы будильника необходимо разрешить показ уведомлений.",
                    buttonText = "Далее",
                    onButtonClick = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )

                1 -> OnboardingPage(
                    icon = Icons.Rounded.Settings,
                    title = "Полный экран",
                    subtitle = "Перейдите в настройки и разрешите показ уведомлений во весь экран, чтобы будильник мог разбудить вас поверх блокировки.",
                    buttonText = "В настройки",
                    onButtonClick = {
                        fullScreenIntentPermissionLauncher.launch(intent)
                    }
                )

                2 -> OnboardingPage(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Всё готово!",
                    subtitle = "Теперь будильник настроен правильно и точно сможет вас разбудить.",
                    buttonText = "Начать",
                    onButtonClick = {
                        onSaveAndFinishOnboarding()
                    }
                )
            }
        }

        // Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }

                val width by animateDpAsState(targetValue = if (pagerState.currentPage == iteration) 24.dp else 10.dp)

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(10.dp)
                        .width(width)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = { onButtonClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview
@Composable
fun Prew() {
    MaterialTheme {
        OnboardingScreen({})
    }
}