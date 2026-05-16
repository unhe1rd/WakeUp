package com.bananchiki.wakeup.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appodeal.ads.Appodeal

/**
 * Compose-обёртка для баннера Appodeal.
 * Используем Appodeal.getBannerView() — официальный способ получить готовый BannerView.
 */
@Composable
fun AppodealBanner(modifier: Modifier = Modifier) {
    val activity = LocalContext.current as Activity
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = {
            // getBannerView возвращает готовый View, которым управляет сам SDK
            val view = Appodeal.getBannerView(activity)
            // Показываем баннер в этом view
            Appodeal.show(activity, Appodeal.BANNER_VIEW)
            view
        }
    )
}
