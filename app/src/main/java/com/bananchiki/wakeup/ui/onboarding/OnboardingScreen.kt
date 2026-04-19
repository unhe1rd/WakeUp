package com.bananchiki.wakeup.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.data.preferences.OnboardingPreferenceManager
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit, onSaveOnboarding: () -> Unit) {
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isSuccess ->
        if (!isSuccess) {
            Toast.makeText(context, "Permission denied. Alarm won't show!", Toast.LENGTH_LONG)
                .show()
        }
    }

    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
        data = Uri.parse("package:${context.packageName}")
    }

    val pagerState = rememberPagerState(pageCount = { 3 })

    Column() {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "Для правильной работы будильника необходимо разрешить показ уведомлений",
                    buttonText = "Далее",
                    onButtonClick = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )

                1 -> OnboardingPage(
                    title = "Для правильной работы будильника необходимо перейти в настройки и разрешить показ уведомлений во весь экран",
                    buttonText = "Перейти в настройки",
                    onButtonClick = {
                        context.startActivity(intent)
                    }
                )

                2 -> OnboardingPage(
                    title = "Теперь всё должно работать как надо!",
                    buttonText = "Готово",
                    onButtonClick = {
                        onSaveOnboarding()
                        onFinish()
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),

            ) {
            repeat(pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            color = if (it == pagerState.currentPage) {
                                Color.Gray
                            } else {
                                Color.DarkGray
                            }
                        )
                        .size(12.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(title: String, buttonText: String, onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            text = title
        )
        Button(
            onClick = { onButtonClick() }
        ) {
            Text(buttonText)
        }
    }
}

@Preview
@Composable
fun Prew() {
    OnboardingScreen({}, {})
}