package com.bananchiki.wakeup.ui.components

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.appodeal.ads.Appodeal
import com.appodeal.ads.NativeCallbacks
import com.appodeal.ads.NativeAd
import com.appodeal.ads.nativead.NativeAdView
import com.bananchiki.wakeup.R
import kotlinx.coroutines.delay

@Composable
fun AppodealNativeAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(Appodeal.isLoaded(Appodeal.NATIVE)) }
    
    // Периодически проверяем статус загрузки (поллинг)
    LaunchedEffect(Unit) {
        while (true) {
            val loaded = Appodeal.isLoaded(Appodeal.NATIVE)
            if (isAdLoaded != loaded) {
                isAdLoaded = loaded
            }
            delay(2000) // проверяем каждые 2 секунды
        }
    }

    val titleColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.toArgb()
    val descColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val btnBgColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.toArgb()
    val btnTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.toArgb()

    if (isAdLoaded) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                val view = LayoutInflater.from(ctx).inflate(R.layout.native_ad_compact, null) as NativeAdView
                val ads = Appodeal.getNativeAds(1)
                if (ads.isNotEmpty()) {
                    view.registerView(ads[0])
                }
                view
            },
            update = { view ->
                // Применяем цвета из текущей Compose-темы (Светлая/Темная)
                view.findViewById<android.widget.TextView>(R.id.titleView)?.setTextColor(titleColor)
                view.findViewById<android.widget.TextView>(R.id.descriptionView)?.setTextColor(descColor)
                view.findViewById<android.widget.Button>(R.id.callToActionView)?.apply {
                    setTextColor(btnTextColor)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(btnBgColor)
                }

                if (Appodeal.isLoaded(Appodeal.NATIVE)) {
                    val ads = Appodeal.getNativeAds(1)
                    if (ads.isNotEmpty()) {
                        view.registerView(ads[0])
                    }
                }
            }
        )
    } else {
        // Временный пустой Spacer, чтобы не прыгал UI, либо лог
        Spacer(modifier = Modifier.height(1.dp))
    }
}
