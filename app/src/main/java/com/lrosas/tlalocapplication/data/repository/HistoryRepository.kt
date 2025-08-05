package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lrosas.tlalocapplication.data.model.Telemetry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HistoryRepository(
    private val db  : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth      = FirebaseAuth.getInstance()
) {

    private fun historyCol(zoneId: String) =
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("zones")
            .document(zoneId)
            .collection("history")



    /**
     * 1) Guarda una lectura completa (pero inyecta el timestamp del servidor).
     */
    suspend fun add(zoneId: String, reading: Telemetry) {
        // Creamos un map con todos los campos de Telemetry menos timestamp,
        // y forzamos serverTimestamp() para que no llegue null.
        val data = mutableMapOf<String, Any?>(
            "zoneId"       to reading.zoneId,
            "humidity"     to reading.humidity,
            "light"        to reading.light,
            "waterLevel"   to reading.waterLevel,
            "waterQuality" to reading.waterQuality,
            "timestamp"    to FieldValue.serverTimestamp()
        )
        // Insertamos como un nuevo documento en history/{auto-id}
        historyCol(zoneId)
            .add(data)
            .await()
    }

    /**
     * Flujo de lista de Telemetry de las últimas 24 h,
     * ordenadas por timestamp ascendente.
     */
    fun latest(zoneId: String): Flow<List<Telemetry>> = callbackFlow {
        // hace 24 h
        val since = com.google.firebase.Timestamp(
            java.util.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        )

        val reg: ListenerRegistration = historyCol(zoneId)
            .whereGreaterThanOrEqualTo("timestamp", since)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) close(err)
                else {
                    val list = snap
                        ?.documents
                        ?.mapNotNull { it.toObject(Telemetry::class.java) }
                        ?: emptyList()
                    trySend(list)
                }
            }

        awaitClose { reg.remove() }
    }
}