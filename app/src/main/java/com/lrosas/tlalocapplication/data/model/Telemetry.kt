package com.lrosas.tlalocapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Telemetry(
    val zoneId: String      = "",        // «zone1», «huertoA», …
    val humidity:     Float? = null,
    val light:        Float? = null,
    val waterLevel:   Float? = null,
    val waterQuality: Float? = null,

    // ► guarda y lee como Timestamp, no como Long
    @ServerTimestamp          // cuando se sube con null → lo rellena Firestore
    val timestamp: Timestamp? = null
)
