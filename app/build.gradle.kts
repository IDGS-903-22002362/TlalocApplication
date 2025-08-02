plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinCompose)
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

    /* ▶️ activa Compose correctamente */
    buildFeatures { compose = true }

    /* ▶️ usa JDK 17 con AGP 8.9 */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    packaging {
        resources {
            // Elimina duplicados típicos de Netty / BouncyCastle / etc.
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/*.kotlin_module",
                "META-INF/io.netty.versions.properties"
            )
        }
    }
}


dependencies {

    // Firebase (BoM impone versiones)
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

// Compose
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.3")

// Concurrency / Data
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")  // Kotlin 2.x compatible :contentReference[oaicite:4]{index=4}
    implementation("androidx.datastore:datastore-preferences:1.1.7")

// Background
    implementation("androidx.work:work-runtime-ktx:2.10.3")                   // SDK 35 ready :contentReference[oaicite:5]{index=5}

// Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")                // último artefacto (pre-release) :contentReference[oaicite:6]{index=6}

// Camera / QR
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

// MQTT
    implementation("com.hivemq:hivemq-mqtt-client:1.3.7")

// Jetpack basics (stables)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

// Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}
