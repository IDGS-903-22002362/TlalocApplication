// data/repository/HistoryRepository.kt

package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.lrosas.tlalocapplication.data.model.Telemetry
import com.lrosas.tlalocapplication.data.model.Reading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class HistoryRepository(
    private val db  : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth      = FirebaseAuth.getInstance()
) {

    /** Base path: /users/{uid}/zones/{zoneId}/history */
    private fun historyCol(zoneId: String) =
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("zones")
            .document(zoneId)
            .collection("history")

    /**
     * 1) Guarda una lectura **completa** (Telemetry) en history/{auto-id},
     *    manteniendo el campo `timestamp` para la consulta.
     */
    suspend fun add(zoneId: String, reading: Telemetry) {
        historyCol(zoneId)
            .add(reading)
            .await()
    }

    /**
     * 2) Devuelve un Flow con las lecturas de las últimas 24 h,
     *    mapeadas a tu modelo `Reading`, ordenadas ascendentemente.
     */
    fun latest(zoneId: String): Flow<List<Reading>> = callbackFlow {
        // Marca el instante “hace 24 h” como Timestamp de Firestore
        val since = Timestamp(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

        val registration: ListenerRegistration = historyCol(zoneId)
            .whereGreaterThanOrEqualTo("timestamp", since)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                } else {
                    // Mapea cada documento a Reading en lugar de Telemetry
                    val list = snap
                        ?.documents
                        ?.mapNotNull { it.toObject(Reading::class.java) }
                        ?: emptyList()
                    trySend(list)
                }
            }

        awaitClose { registration.remove() }
    }
}
