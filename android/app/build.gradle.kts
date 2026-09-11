plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.hoscat.mtj.dev"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.hoscat.mtj.dev"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-dev"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    signingConfigs.getByName("debug") {
        storeFile = File(requireNotNull(System.getenv("ANDROID_USER_HOME")), "debug.keystore")
    }
    sourceSets["main"].java.srcDirs("../vendor", "../../saju/src", "../../tarot/src", "../../records/src", "../../design/native")
    sourceSets["main"].java.exclude("**/._*", "**/* 2.kt")
    androidResources { ignoreAssetsPattern = "!.*:!._*:!.DS_Store" }
    packaging { resources.excludes += "**/._*" }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.foundation:foundation")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("junit:junit:4.13.2")
}
