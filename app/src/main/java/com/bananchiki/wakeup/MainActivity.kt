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
import com.bananchiki.wakeup.billing.BillingManager
import com.bananchiki.wakeup.billing.PremiumManager
import com.bananchiki.wakeup.billing.SubscriptionState
import androidx.compose.runtime.LaunchedEffect
import com.bananchiki.wakeup.data.preferences.ThemePreferenceManager
import com.bananchiki.wakeup.data.preferences.ThemeSettings
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bananchiki.wakeup.data.preferences.OnboardingPreferenceManager
import com.bananchiki.wakeup.data.work.GreetingsWorker
import com.bananchiki.wakeup.ui.components.AddAlarmDialog
import com.bananchiki.wakeup.ui.components.AppodealNativeAd
import com.bananchiki.wakeup.ui.components.BottomNavBar
import androidx.compose.foundation.layout.Column
import com.bananchiki.wakeup.ui.home.HomeScreen
import com.bananchiki.wakeup.ui.home.HomeViewModel
import com.bananchiki.wakeup.ui.onboarding.OnboardingScreen
import com.bananchiki.wakeup.ui.paywall.PaywallScreen
import com.bananchiki.wakeup.ui.progress.ProgressScreen
import com.bananchiki.wakeup.ui.goals.AchievementsScreen
import com.bananchiki.wakeup.ui.theme.WakeUpTheme
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var billingManager: BillingManager
    private lateinit var premiumManager: PremiumManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreferenceManager = ThemePreferenceManager(applicationContext)
        val onboardingPreferenceManager = OnboardingPreferenceManager(applicationContext)
        premiumManager = PremiumManager(applicationContext)
        billingManager = BillingManager(applicationContext, premiumManager)
        billingManager.startConnection()

        // Appodeal SDK init
        Appodeal.setNativeCallbacks(object : com.appodeal.ads.NativeCallbacks {
            override fun onNativeLoaded() { android.util.Log.d("Appodeal", "Native Ad Loaded") }
            override fun onNativeFailedToLoad() { android.util.Log.d("Appodeal", "Native Ad Failed to Load") }
            override fun onNativeShown(nativeAd: com.appodeal.ads.NativeAd?) { android.util.Log.d("Appodeal", "Native Ad Shown") }
            override fun onNativeShowFailed(nativeAd: com.appodeal.ads.NativeAd?) {}
            override fun onNativeClicked(nativeAd: com.appodeal.ads.NativeAd?) {}
            override fun onNativeExpired() {}
        })

        Appodeal.setRewardedVideoCallbacks(object : com.appodeal.ads.RewardedVideoCallbacks {
            override fun onRewardedVideoLoaded(isPrecache: Boolean) { android.util.Log.d("Appodeal", "Rewarded Video Loaded") }
            override fun onRewardedVideoFailedToLoad() { android.util.Log.d("Appodeal", "Rewarded Video Failed to Load") }
            override fun onRewardedVideoShown() {}
            override fun onRewardedVideoShowFailed() {}
            override fun onRewardedVideoClicked() {}
            override fun onRewardedVideoFinished(amount: Double, currency: String) {
                android.util.Log.d("Appodeal", "Rewarded Video Finished! Amount: $amount")
            }
            override fun onRewardedVideoClosed(finished: Boolean) {}
            override fun onRewardedVideoExpired() {}
        })

        Appodeal.initialize(
            context = this,
            appKey = "a5d8ef2ffeeb18b5bf53fa46be4d48737602cfbaf6657652",
            adTypes = Appodeal.NATIVE or Appodeal.REWARDED_VIDEO,
            callback = object : ApdInitializationCallback {
                override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                    android.util.Log.d("Appodeal", "Initialization Finished. Errors: $errors")
                }
            }
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val saveRequest =
            PeriodicWorkRequestBuilder<GreetingsWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "Greeting Work",
            ExistingPeriodicWorkPolicy.KEEP,
            saveRequest
        )

        setContent {
            val themeSettings by themePreferenceManager.themeFlow.collectAsState(initial = ThemeSettings.SYSTEM)
            val isPremium by premiumManager.isPremiumFlow.collectAsState(initial = false)
            val subscriptionState by billingManager.subscriptionState.collectAsState()
            val isFirstLaunch by onboardingPreferenceManager.onboardingFlow.collectAsState(false)

            // Sync billing state with premium manager
            val coroutineScope = rememberCoroutineScope()
            
            androidx.compose.runtime.LaunchedEffect(subscriptionState) {
                when (subscriptionState) {
                    SubscriptionState.PURCHASED -> premiumManager.updatePremiumStatus(true)
                    SubscriptionState.NOT_PURCHASED -> premiumManager.updatePremiumStatus(false)
                    else -> { /* PENDING or ERROR — keep current state */ }
                }
            }
            
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
                        if(currentRoute != "onboarding" && currentRoute != "paywall") {
                            Column {
                                // Показываем рекламу всем пользователям, так как подписка временно отключена
                                AppodealNativeAd()
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
                                    onGoalsClick = {
                                        navController.navigate("goals") {
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
                                },
                                isPremium = isPremium,
                                onProClick = {
                                    navController.navigate("paywall") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable("progress") {
                            ProgressScreen(
                                isPremium = isPremium,
                                onProClick = {
                                    navController.navigate("paywall") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable("goals") {
                            AchievementsScreen(
                                isPremium = isPremium,
                                onProClick = {
                                    navController.navigate("paywall") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            com.bananchiki.wakeup.ui.settings.SettingsScreen(
                                currentTheme = themeSettings,
                                onThemeSelected = { newTheme ->
                                    coroutineScope.launch {
                                        themePreferenceManager.saveTheme(newTheme)
                                    }
                                },
                                isPremium = isPremium,
                                onProClick = {
                                    navController.navigate("paywall") {
                                        launchSingleTop = true
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
                        composable("paywall") {
                            val isMock by billingManager.useMockBilling.collectAsState()
                            PaywallScreen(
                                onDismiss = { navController.popBackStack() },
                                onMonthlyClick = {
                                    coroutineScope.launch {
                                        premiumManager.updatePremiumStatus(true)
                                        navController.popBackStack()
                                    }
                                },
                                onYearlyClick = {
                                    coroutineScope.launch {
                                        premiumManager.updatePremiumStatus(true)
                                        navController.popBackStack()
                                    }
                                },
                                onRestoreClick = {
                                    billingManager.restorePurchases()
                                },
                                isMockBilling = isMock,
                                onMockPurchase = {
                                    billingManager.debugSimulatePurchase()
                                    navController.popBackStack()
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

    override fun onDestroy() {
        super.onDestroy()
        if (::billingManager.isInitialized) {
            billingManager.endConnection()
        }
    }

}
