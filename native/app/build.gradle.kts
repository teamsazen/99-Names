import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services") apply false
    id("app.cash.paparazzi")
}

// Firebase config is not in the public repo; without it the app builds with
// analytics off.
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

// Upload key for Play Store builds: native/key.properties (not committed). Without
// it, release builds fall back to the debug key.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("key.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.muzamil.asmaulhusna"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }

    defaultConfig {
        applicationId = "com.muzamil.asmaulhusna"
        minSdk = 24
        targetSdk = 36
        // Above the Flutter app's 1.0.0+2, so it updates in place.
        versionCode = 3
        versionName = "2.0.0"
        // Analytics stays off in debug builds.
        manifestPlaceholders["analyticsEnabled"] = "false"
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
            manifestPlaceholders["analyticsEnabled"] = "true"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/*.version",
            "META-INF/**/*.kotlin_builtins",
            "kotlin/**",
            "DebugProbesKt.bin",
            "META-INF/com.android.tools/**",
        )
    }

    // Only English UI strings ship from libraries.
    androidResources {
        localeFilters += listOf("en")
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // Compose without Material: the app draws its own controls, like the Flutter app.
    val compose = platform("androidx.compose:compose-bom:2026.11.01")
    implementation(compose)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    implementation(platform("com.google.firebase:firebase-bom:35.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    constraints {
        // Play services and in-app updates still ask for fragment 1.1.0, which Play Console
        // flags as outdated; lift it. Not a new library: only its version changes.
        implementation("androidx.fragment:fragment:1.9.1")
    }

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20260814")
}
