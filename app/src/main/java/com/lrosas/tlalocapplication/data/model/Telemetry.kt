// Telemetry.kt (opcional — solo para parse local; Firestore no necesita clase)
package com.lrosas.tlalocapplication.data.model

data class Telemetry(
    val plantId: String = "",
    val timestamp: Long = 0L,
    val humidity: Int = 0,
    val light: Float = 0f,
    val waterQuality: Float = 0f,
    val waterLevel: Float = 0f
)
