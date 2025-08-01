plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.lrosas.tlalocapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lrosas.tlalocapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")
    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    /* ---------- Concurrencia y datos ---------- */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")  // 1.8.1 May-10-24:contentReference[oaicite:8]{index=8}
    implementation("androidx.datastore:datastore-preferences:1.1.7")          // estable May-20-25:contentReference[oaicite:9]{index=9}
    /* ---------- Background ---------- */
    implementation("androidx.work:work-runtime-ktx:2.10.3")                   // estable Jul-30-25:contentReference[oaicite:11]{index=11}

    /* ---------- Gráficas ---------- */
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")                // última release:contentReference[oaicite:12]{index=12}

    /* ---------- Cámaras / QR ---------- */
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")            // 4.3.0 docs:contentReference[oaicite:13]{index=13}

    /* ---------- MQTT (opcional) ---------- */
    implementation("com.hivemq:hivemq-mqtt-client:1.3.7")                     // 1.3.7 Jun-11-25:contentReference[oaicite:14]{index=14}

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}