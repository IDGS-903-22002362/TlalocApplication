package com.lrosas.tlalocapplication.data.model

import com.google.firebase.Timestamp

data class Reading(
    val t: Timestamp = Timestamp.now(),   // id del doc
    val humPct: Int = 0,
    val lux: Float = 0f,
    val tds: Float = 0f,
    val dist: Float = 0f,
    val pumpOn: Boolean = false
)