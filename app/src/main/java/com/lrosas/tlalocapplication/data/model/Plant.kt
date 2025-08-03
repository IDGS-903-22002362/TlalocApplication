package com.lrosas.tlalocapplication.data.model


import com.google.firebase.firestore.DocumentId

data class Plant(
    @DocumentId
    val id: String = "",
    val name: String = ""
)
