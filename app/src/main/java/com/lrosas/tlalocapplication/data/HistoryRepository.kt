package com.lrosas.tlalocapplication.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.lrosas.tlalocapplication.data.model.Reading
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object HistoryRepository {

    /** Devuelve las últimas N lecturas de la zona (Firestore) */
    fun latest(zoneId: String, limit: Long = 96): Flow<List<Reading>> = flow {
        val snap = Firebase.firestore
            .collection("zones").document(zoneId)
            .collection("readings")
            .orderBy("t", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        emit(snap.toObjects(Reading::class.java).reversed()) // en orden cronológico
    }
}