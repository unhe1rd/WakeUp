package com.bananchiki.wakeup

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bananchiki.wakeup.ui.alarm.AlarmScreen
import com.bananchiki.wakeup.ui.theme.WakeUpTheme
import android.media.ToneGenerator
import com.bananchiki.wakeup.data.AchievementManager

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: android.media.MediaPlayer? = null
    private var fallbackRingtone: android.media.Ringtone? = null
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show Activity over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

        volumeControlStream = android.media.AudioManager.STREAM_ALARM

        startAlarmEffects()

        setContent {
            WakeUpTheme {
                val taskType = intent.getStringExtra("task_type") ?: "NONE"
                val alarmLabel = intent.getStringExtra("alarm_label") ?: "Wake up!"
                
                AlarmScreen(
                    label = alarmLabel,
                    taskType = taskType,
                    onDismiss = { dismissAlarm() }
                )
            }
        }
    }

    private fun startAlarmEffects() {
        // Проверяем и устанавливаем громкость, если она на нуле
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_ALARM)
            if (currentVolume == 0) {
                val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, maxVolume / 2, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sound
        var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(this@AlarmActivity, alarmUri)
                val attributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(attributes)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback if MediaPlayer fails
            try {
                fallbackRingtone = RingtoneManager.getRingtone(this, alarmUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    fallbackRingtone?.isLooping = true
                    fallbackRingtone?.audioAttributes = android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    fallbackRingtone?.streamType = android.media.AudioManager.STREAM_ALARM
                }
                fallbackRingtone?.play()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }

            // Ultimate fallback if RingtoneManager also fails or no sound is available
            try {
                toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 60000) // 1 min
            } catch (e3: Exception) {
                e3.printStackTrace()
            }
        }

        // Vibration
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun dismissAlarm() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fallbackRingtone?.stop()
        toneGenerator?.stopTone()
        toneGenerator?.release()
        toneGenerator = null
        vibrator?.cancel()

        AchievementManager.registerWakeUp(this)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        val alarmId = intent.getIntExtra("alarm_id", 0)
        notificationManager.cancel(alarmId)

        finish()
    }

    override fun onDestroy() {
        dismissAlarm()
        super.onDestroy()
    }
}
