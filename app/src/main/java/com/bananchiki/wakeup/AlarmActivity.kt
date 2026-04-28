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

class AlarmActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
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
        // Sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, alarmUri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()
        } else {
            @Suppress("DEPRECATION")
            ringtone?.streamType = android.media.AudioManager.STREAM_ALARM
        }

        ringtone?.play()

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
        ringtone?.stop()
        vibrator?.cancel()

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
