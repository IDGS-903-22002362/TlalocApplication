// Zone.kt  ───────  subcolección users/{uid}/zones
package com.lrosas.tlalocapplication.data.model
import com.google.firebase.firestore.DocumentId

data class Zone(
    @DocumentId val id: String = "",
    val name: String = "",
    val plantId: String = "",
    val auto    : Boolean = true
)
