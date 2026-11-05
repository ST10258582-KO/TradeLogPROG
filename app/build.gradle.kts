plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.tradelog.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tradelog.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Store credentials in keystore.properties (not committed to git)
            // keytoreFile, storePassword, keyAlias, keyPassword loaded at build time
            // Reference: Google LLC, "Sign your app" Android Developers (2024)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Enable desugaring for java.time APIs on older Android versions
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    // Networking — Retrofit + OkHttp (Square Inc.)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    // Async — Kotlin Coroutines (JetBrains)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    // Settings persistence — DataStore (Google LLC, 2024)
    implementation(libs.datastore.preferences)
    // Firebase — Auth, Firestore, Storage, Messaging (Google LLC, 2024)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.auth)
    // Google Sign-In — Credential Manager (Google LLC, 2024)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play.services)
    // Room — offline-first local database with KSP code generation (Google LLC, 2024)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // WorkManager — background data synchronisation (Google LLC, 2024)
    implementation(libs.androidx.work.runtime)
    // Biometric — fingerprint / face authentication (Google LLC, 2024)
    implementation(libs.androidx.biometric)
    // Coil — async image loading for trade screenshots (Coil Contributors, 2024)
    implementation(libs.coil.compose)
    // Desugaring — java.time support on API < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
