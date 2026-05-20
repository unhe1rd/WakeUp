package com.bananchiki.wakeup

import android.content.Context
import android.media.ToneGenerator
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
import com.bananchiki.wakeup.data.AchievementManager
import com.bananchiki.wakeup.data.preferences.RingtonePreferenceManager

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: android.media.MediaPlayer? = null
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

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

        // Force alarm AND music volume up (emulator often has alarm stream muted)
        try {
            val alarmMax = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
            val musicMax = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            if (audioManager.getStreamVolume(android.media.AudioManager.STREAM_ALARM) == 0) {
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, alarmMax / 2, 0)
            }
            if (audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) == 0) {
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, musicMax / 2, 0)
            }
        } catch (e: Exception) {
            android.util.Log.e("AlarmActivity", "Failed to set volume", e)
        }

        // Get selected ringtone from preferences
        val ringtoneManager = RingtonePreferenceManager(this)
        val selectedSound = ringtoneManager.getSelectedRingtoneSync()
        val soundResId = selectedSound.rawResId

        val alarmAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val mediaAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        var soundStarted = false

        // Attempt 1: Play with USAGE_ALARM
        try {
            val player = android.media.MediaPlayer.create(this, soundResId)
            if (player != null) {
                player.setAudioAttributes(alarmAttributes)
                player.isLooping = true
                player.start()
                mediaPlayer = player
                soundStarted = true
                android.util.Log.d("AlarmActivity", "✅ Playing ${selectedSound.name} with USAGE_ALARM")
            }
        } catch (e: Exception) {
            android.util.Log.e("AlarmActivity", "❌ USAGE_ALARM failed", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }

        // Attempt 2: USAGE_MEDIA fallback (emulator)
        if (!soundStarted) {
            try {
                val player = android.media.MediaPlayer.create(this, soundResId)
                if (player != null) {
                    player.setAudioAttributes(mediaAttributes)
                    player.isLooping = true
                    player.start()
                    mediaPlayer = player
                    soundStarted = true
                    android.util.Log.d("AlarmActivity", "✅ Playing ${selectedSound.name} with USAGE_MEDIA")
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmActivity", "❌ USAGE_MEDIA failed", e)
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

        // Attempt 3: ToneGenerator
        if (!soundStarted) {
            try {
                toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 60000)
            } catch (e: Exception) {
                android.util.Log.e("AlarmActivity", "❌ ToneGenerator failed", e)
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
