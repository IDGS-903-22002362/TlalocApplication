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
    /* ---------- Firebase BOM (usa versiones internas) ---------- */
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore") // ya no pongas versión aquí
    implementation("com.google.firebase:firebase-messaging")

    /* ---------- Compose & Navegación ---------- */
    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("androidx.compose.material:material-icons-extended")

    /* ---------- Concurrencia y datos ---------- */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    /* ---------- Background ---------- */
    implementation("androidx.work:work-runtime-ktx:2.10.3")

    /* ---------- Gráficas ---------- */
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    /* ---------- Cámaras / QR ---------- */
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    /* ---------- MQTT (opcional) ---------- */
    implementation("com.hivemq:hivemq-mqtt-client:1.3.7")

    /* ---------- Android Jetpack básicos ---------- */
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    /* ---------- Test ---------- */
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
