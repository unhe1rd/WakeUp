package com.bananchiki.wakeup

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import com.bananchiki.wakeup.data.preferences.ThemePreferenceManager
import com.bananchiki.wakeup.data.preferences.ThemeSettings
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bananchiki.wakeup.data.preferences.OnboardingPreferenceManager
import com.bananchiki.wakeup.ui.components.AddAlarmDialog
import com.bananchiki.wakeup.ui.components.BottomNavBar
import com.bananchiki.wakeup.ui.home.HomeScreen
import com.bananchiki.wakeup.ui.home.HomeViewModel
import com.bananchiki.wakeup.ui.onboarding.OnboardingScreen
import com.bananchiki.wakeup.ui.progress.ProgressScreen
import com.bananchiki.wakeup.ui.theme.WakeUpTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    /*private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permission denied. Alarm won't show!", Toast.LENGTH_LONG).show()
        }
    } */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreferenceManager = ThemePreferenceManager(applicationContext)
        val onboardingPreferenceManager = OnboardingPreferenceManager(applicationContext)


        setContent {
            val themeSettings by themePreferenceManager.themeFlow.collectAsState(initial = ThemeSettings.SYSTEM)
            val isFirstLaunch by onboardingPreferenceManager.onboardingFlow.collectAsState(false)

            val coroutineScope = rememberCoroutineScope()
            
            val useDarkTheme = when (themeSettings) {
                ThemeSettings.LIGHT -> false
                ThemeSettings.DARK -> true
                ThemeSettings.SYSTEM -> isSystemInDarkTheme()
            }

            WakeUpTheme(darkTheme = useDarkTheme) {
                val alarms by viewModel.allAlarms.collectAsState(initial = emptyList())
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"
                var showAddDialog by remember { mutableStateOf(false) }
                var alarmBeingEdited by remember { mutableStateOf<com.bananchiki.wakeup.data.model.Alarm?>(null) }

                LaunchedEffect(isFirstLaunch) {
                    if (isFirstLaunch){
                        navController.navigate("onboarding"){
                            popUpTo("home"){
                                inclusive = true
                            }
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if(currentRoute != "onboarding") {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onAddClick = {
                                    alarmBeingEdited = null
                                    showAddDialog = true
                                },
                                onProgressClick = {
                                    navController.navigate("progress") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onHomeClick = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onSettingsClick = {
                                    navController.navigate("settings") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController, 
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                alarms = alarms,
                                onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
                                onToggleAlarm = { alarm, isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                                onEditAlarm = { alarm ->
                                    alarmBeingEdited = alarm
                                    showAddDialog = true
                                }
                            )
                        }
                        composable("progress") {
                            ProgressScreen()
                        }
                        composable("settings") {
                            com.bananchiki.wakeup.ui.settings.SettingsScreen(
                                currentTheme = themeSettings,
                                onThemeSelected = { newTheme ->
                                    coroutineScope.launch {
                                        themePreferenceManager.saveTheme(newTheme)
                                    }
                                }
                            )
                        }
                        composable("onboarding"){
                            OnboardingScreen(
                                onSaveAndFinishOnboarding = {
                                    coroutineScope.launch {
                                        onboardingPreferenceManager.saveOnboarding()
                                        navController.navigate("home"){
                                            popUpTo("onboarding"){
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                if (showAddDialog) {
                    AddAlarmDialog(
                        alarmToEdit = alarmBeingEdited,
                        onDismiss = { 
                            showAddDialog = false
                            alarmBeingEdited = null
                        },
                        onConfirm = { hour, minute, label, days, taskType ->
                            if (alarmBeingEdited != null) {
                                viewModel.editAlarm(alarmBeingEdited!!, hour, minute, label, days, taskType)
                            } else {
                                viewModel.addAlarm(hour, minute, label, days, taskType)
                            }
                            showAddDialog = false
                            alarmBeingEdited = null
                        }
                    )
                }
            }
        }
    }

    /*private fun checkAndRequestNotificationsPermission() {
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

    private fun checkAndRequestFullScreenPermission(){
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (!notificationManager.canUseFullScreenIntent()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = android.net.Uri.parse("package:${packageName}")
                }
                this.startActivity(intent)
            }
        }
    } */
}
