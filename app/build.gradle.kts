import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    kotlin("plugin.serialization") version "2.0.21"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.bananchiki.wakeup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bananchiki.wakeup"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = localProperties.getProperty("OPENROUTER_API_KEY") ?: ""
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.text)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // Ktor
    val ktorVersion = "2.3.12"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    val work_version = "2.9.0"
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // Ads
    implementation("com.appodeal.ads.sdk:core:4.1.0")

    // Bidon
    implementation("org.bidon:amazon-adapter:11.1.1.0")
    implementation("org.bidon:applovin-adapter:13.5.1.0")
    implementation("org.bidon:bidmachine-adapter:3.7.0.0")
    implementation("org.bidon:bigoads-adapter:5.6.2.0")
    implementation("org.bidon:chartboost-adapter:9.10.2.0")
    implementation("org.bidon:dtexchange-adapter:8.4.1.0")
    implementation("org.bidon:inmobi-adapter:11.1.0.0")
    implementation("org.bidon:ironsource-adapter:9.4.2.0")
    implementation("org.bidon:meta-adapter:6.20.0.0")
    implementation("org.bidon:mintegral-adapter:17.0.31.0")
    implementation("org.bidon:mobilefuse-adapter:1.9.3.0")
    implementation("org.bidon:moloco-adapter:4.3.1.0")
    implementation("org.bidon:startio-adapter:5.2.4.1")
    implementation("org.bidon:taurusx-adapter:1.12.2.0")
    implementation("org.bidon:unityads-adapter:4.17.0.0")
    implementation("org.bidon:vkads-adapter:5.27.4.0")
    implementation("org.bidon:vungle-adapter:7.6.1.0")
    implementation("org.bidon:yandex-adapter:7.17.0.0")
    implementation("org.bidon:zmaticoo-adapter:2.0.5.1.0")

    // AppLovin MAX
    implementation("com.applovin.mediation:amazon-tam-adapter:11.1.1.0")
    implementation("com.applovin.mediation:bidmachine-adapter:3.7.0.0")
    implementation("com.applovin.mediation:bigoads-adapter:5.6.2.0")
    implementation("com.applovin.mediation:bytedance-adapter:7.7.0.2.0")
    implementation("com.applovin.mediation:chartboost-adapter:9.10.2.0")
    implementation("com.applovin.mediation:facebook-adapter:6.20.0.0")
    implementation("com.applovin.mediation:fyber-adapter:8.4.1.0")
    implementation("com.applovin.mediation:google-ad-manager-adapter:24.7.0.0")
    implementation("com.applovin.mediation:google-adapter:24.7.0.0")
    implementation("com.applovin.mediation:inmobi-adapter:11.1.0.0")
    implementation("com.applovin.mediation:ironsource-adapter:9.4.2.0.0")
    implementation("com.applovin.mediation:mintegral-adapter:17.0.31.0")
    implementation("com.applovin.mediation:mobilefuse-adapter:1.9.3.0")
    implementation("com.applovin.mediation:moloco-adapter:4.3.1.0")
    implementation("com.applovin.mediation:mytarget-adapter:5.27.4.0")
    implementation("com.applovin.mediation:smaato-adapter:22.7.2.3")
    implementation("com.applovin.mediation:unityads-adapter:4.17.0.0")
    implementation("com.applovin.mediation:verve-adapter:3.7.1.0")
    implementation("com.applovin.mediation:vungle-adapter:7.6.1.0")
    implementation("com.applovin.mediation:yandex-adapter:7.17.0.0")

    // Appodeal
    implementation("com.appodeal.ads.sdk.adapters:adjust:5.4.6.1")
    implementation("com.appodeal.ads.sdk.adapters:amazon:11.1.1.1")
    implementation("com.appodeal.ads.sdk.adapters:applovin:13.5.1.0")
    implementation("com.appodeal.ads.sdk.adapters:applovin_max:13.5.1.1")
    implementation("com.appodeal.ads.sdk.adapters:appsflyer:6.17.3.1")
    implementation("com.appodeal.ads.sdk.adapters:bidmachine:3.7.0.0")
    implementation("com.appodeal.ads.sdk.adapters:bidon:0.13.0.0")
    implementation("com.appodeal.ads.sdk.adapters:bigo_ads:5.6.2.0")
    implementation("com.appodeal.ads.sdk.adapters:chartboost:9.10.2.0")
    implementation("com.appodeal.ads.sdk.adapters:dt_exchange:8.4.1.0")
    implementation("com.appodeal.ads.sdk.adapters:facebook_analytics:18.0.3.0")
    implementation("com.appodeal.ads.sdk.adapters:firebase:23.0.0.1")
    implementation("com.appodeal.ads.sdk.adapters:iab:1.8.1.0")
    implementation("com.appodeal.ads.sdk.adapters:inmobi:11.1.0.0")
    implementation("com.appodeal.ads.sdk.adapters:ironsource:9.4.2.0")
    implementation("com.appodeal.ads.sdk.adapters:level_play:9.4.2.0")
    implementation("com.appodeal.ads.sdk.adapters:meta:6.20.0.0")
    implementation("com.appodeal.ads.sdk.adapters:mintegral:17.0.31.0")
    implementation("com.appodeal.ads.sdk.adapters:mobilefuse:1.9.3.0")
    implementation("com.appodeal.ads.sdk.adapters:moloco:4.3.1.0")
    implementation("com.appodeal.ads.sdk.adapters:my_target:5.27.4.0")
    implementation("com.appodeal.ads.sdk.adapters:pangle:7.7.0.2.0")
    implementation("com.appodeal.ads.sdk.adapters:sentry_analytics:8.26.0.0")
    implementation("com.appodeal.ads.sdk.adapters:smaato:22.7.2.0")
    implementation("com.appodeal.ads.sdk.adapters:startio:5.2.4.0")
    implementation("com.appodeal.ads.sdk.adapters:taurusx:1.12.2.0")
    implementation("com.appodeal.ads.sdk.adapters:unity_ads:4.17.0.0")
    implementation("com.appodeal.ads.sdk.adapters:verve:3.7.1.0")
    implementation("com.appodeal.ads.sdk.adapters:vungle:7.6.1.0")
    implementation("com.appodeal.ads.sdk.adapters:yandex:7.17.0.0")

    // Level Play
    implementation("com.unity3d.ads-mediation:bidmachine-adapter:5.5.0")
    implementation("com.unity3d.ads-mediation:bigo-adapter:5.3.0")
    implementation("com.unity3d.ads-mediation:fyber-adapter:5.3.0")
    implementation("com.unity3d.ads-mediation:mintegral-adapter:5.6.0")
    implementation("com.unity3d.ads-mediation:moloco-adapter:5.6.0")
    implementation("com.unity3d.ads-mediation:unityads-adapter:5.6.0")
    implementation("com.unity3d.ads-mediation:verve-adapter:5.2.0")

    // BidMachine
    implementation("io.bidmachine:ads.networks.meta_audience:6.20.0.1")
    implementation("io.bidmachine:ads.networks.mintegral:17.0.31.1")
    implementation("io.bidmachine:ads.networks.my_target:5.27.4.1")
    implementation("io.bidmachine:ads.networks.pangle:7.7.0.2.1")
    implementation("io.bidmachine:ads.networks.vungle:7.6.1.1")
}
