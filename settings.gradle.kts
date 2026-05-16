pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Pangle должен быть первым, чтобы избежать 401 Unauthorized от Appodeal
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") }
        maven { url = uri("https://android-sdk.is.com/") }
        maven { url = uri("https://artifactory.bidmachine.io/bidmachine") }
        maven { url = uri("https://s3.amazonaws.com/smaato-sdk-releases/") }
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven { url = uri("https://artifactory.appodeal.com/appodeal") }
    }
}

rootProject.name = "WakeUp"
include(":app")
