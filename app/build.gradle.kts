plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val keystorePath = System.getenv("KEYSTORE_PATH") as String?
    ?: project.findProperty("KEYSTORE_PATH") as String?
val keystorePasswordValue = System.getenv("KEYSTORE_PASSWORD") as String?
    ?: project.findProperty("KEYSTORE_PASSWORD") as String?
val keyAliasValue = System.getenv("KEY_ALIAS") as String?
    ?: project.findProperty("KEY_ALIAS") as String?
val keyPasswordValue = System.getenv("KEY_PASSWORD") as String?
    ?: project.findProperty("KEY_PASSWORD") as String?
val enableReleaseSigning = listOf(
    keystorePath,
    keystorePasswordValue,
    keyAliasValue,
    keyPasswordValue
).all { !it.isNullOrBlank() }

android {
    namespace = "dev.mlzzen.kaiqiu"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "dev.mlzzen.kaiqiu"
        minSdk = 30
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (enableReleaseSigning) {
            create("release") {
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
                storeFile = rootProject.file(keystorePath!!)
                storePassword = keystorePasswordValue
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            // 使用自定义调试签名
            signingConfig = signingConfigs.create("customDebug") {
                keyAlias = "kaiqiu"
                keyPassword = "kaiqiu123"
                storeFile = rootProject.file("debug.keystore")
                storePassword = "kaiqiu123"
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (enableReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                project.logger.lifecycle("Release signing disabled: missing KEYSTORE_* env or Gradle properties.")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Image
    implementation(libs.coil)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // JSON
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
