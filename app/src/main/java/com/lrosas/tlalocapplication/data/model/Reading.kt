package com.lrosas.tlalocapplication.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.Timestamp

/**
 * Modelo simplificado para el gráfico de historial.
 * Aquí guardamos la humedad como porcentaje entero y el timestamp.
 *
 * Los valores por defecto garantizan un constructor sin-args
 * que Firestore necesita para deserializar.
 */
@IgnoreExtraProperties
data class Reading(
    val timestamp: Timestamp? = null,
    val humPct: Int = 0
)
