package com.bananchiki.wakeup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.bananchiki.wakeup.ui.home.HomeScreen
import com.bananchiki.wakeup.ui.home.HomeViewModel
import com.bananchiki.wakeup.ui.theme.WakeUpTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permission denied. Alarm won't show!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestNotificationsPermission()

        setContent {
            WakeUpTheme {
                val alarms by viewModel.allAlarms.collectAsState(initial = emptyList())
                HomeScreen(
                    alarms = alarms,
                    onAddAlarm = { hour, minute, label, daysOfWeek ->
                        viewModel.addAlarm(hour, minute, label, daysOfWeek)
                    },
                    onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
                    onToggleAlarm = { alarm, isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) }
                )
            }
        }
    }

    private fun checkAndRequestNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
