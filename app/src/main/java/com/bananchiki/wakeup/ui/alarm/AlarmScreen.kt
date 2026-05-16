package com.bananchiki.wakeup.ui.alarm

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import com.bananchiki.wakeup.data.ai.TextGenerator
import com.bananchiki.wakeup.ui.theme.Amber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.bananchiki.wakeup.data.preferences.GreetingCacheManager
import kotlinx.coroutines.flow.first
import android.speech.tts.TextToSpeech
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AlarmScreen(onDismiss: () -> Unit, label: String = "Wake up!", taskType: String) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val cacheManager = remember { GreetingCacheManager(context) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var greetingText by remember { mutableStateOf("Доброе утро, пора вставать!") }
    
    // Состояние для проверки, загружено ли видео
    var isRewardedLoaded by remember { mutableStateOf(Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) }

    if (taskType == "AI") {
        DisposableEffect(context) {
            val textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                }
            }
            tts = textToSpeech

            onDispose {
                textToSpeech.stop()
                textToSpeech.shutdown()
            }
        }

        LaunchedEffect(isTtsReady) {
            if (!isTtsReady) return@LaunchedEffect
            val currentSet = cacheManager.greetingsFlow().first()
            val singleGreeting = currentSet.firstOrNull()
            greetingText = singleGreeting ?: "Доброе утро, пора вставать!"
            val textToSpeak = singleGreeting ?: "Доброе утро, пора вставать!"

            tts?.language = Locale("ru", "RU")
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)

            if (singleGreeting != null) {
                cacheManager.removeGreeting(singleGreeting)
            }
        }
    }

    LaunchedEffect(Unit) {
        // Подписываемся на коллбеки rewarded video (чтобы закрыть будильник после просмотра)
        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
            override fun onRewardedVideoLoaded(isPrecache: Boolean) { isRewardedLoaded = true }
            override fun onRewardedVideoFailedToLoad() { isRewardedLoaded = false }
            override fun onRewardedVideoShown() {}
            override fun onRewardedVideoShowFailed() {}
            override fun onRewardedVideoClicked() {}
            override fun onRewardedVideoFinished(amount: Double, currency: String) {
                android.util.Log.d("Appodeal", "Rewarded Video Finished! Dismissing alarm.")
                // ПОЛЬЗОВАТЕЛЬ ДОСМОТРЕЛ — ВЫКЛЮЧАЕМ БУДИЛЬНИК
                onDismiss()
            }
            override fun onRewardedVideoClosed(finished: Boolean) {}
            override fun onRewardedVideoExpired() { isRewardedLoaded = false }
        })

        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance().time
            val loaded = Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)
            if (isRewardedLoaded != loaded) {
                android.util.Log.d("Appodeal", "Rewarded Video Load State Changed: $loaded")
                isRewardedLoaded = loaded
            }
        }
    }

    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE,  dd MMM", Locale.getDefault())

    val timeString = timeFormat.format(currentTime)
    val dateString = dateFormat.format(currentTime)

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF8C7A),
            Color(0xFFD889E6),
            Color(0xFFE5F0FF),
            Color(0xFFFFFFFF)
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

            Text(
                text = "⛅",
                fontSize = 72.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dateString,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = timeString,
                color = Color.White,
                fontSize = 110.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = label,
                color = Color(0xFF333333),
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold
            )

            when (taskType) {
                "MATH" -> {
                    Spacer(Modifier.weight(1f))
                    MathTaskScreen(onComplete = onDismiss)
                }
                "MEMORY" -> {
                    Spacer(Modifier.weight(1f))
                    MemoryTaskScreen(onComplete = onDismiss)
                }
                "AI" -> {
                    AiGreetingTaskScreen(onComplete = onDismiss, greetingText = greetingText)
                }
                else -> {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { onDismiss() },
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

                    if (isRewardedLoaded && activity != null) {
                        Button(
                            onClick = { Appodeal.show(activity, Appodeal.REWARDED_VIDEO) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C63FF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Watch Ad to Dismiss 🎬",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber,
                            contentColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text(
                            text = "Выключить",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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

