package com.lrosas.tlalocapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Telemetry(
    val zoneId:       String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val humidity:     Float? = null,      // %
    val light:        Float? = null,      // lx
    val waterQuality: Float? = null,      // ppm
    val waterLevel:   Float? = null       // cm
)
