// Care.kt  ───────  colección cares
package com.lrosas.tlalocapplication.data.model
import com.google.firebase.firestore.DocumentId

data class Care(
    @DocumentId val id: String = "",
    val plantId: String = "",
    val humidity: Int = 50,          // % suelo ideal
    val light: Float = 1000f,        // lux ideales
    val waterQuality: Float = 0f,    // ppm TDS ideales
    val waterLevelIdeal: Float = 0f  // cm nivel depósito
)
