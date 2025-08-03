/* data/repository/TelemetryRepository.kt */

package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lrosas.tlalocapplication.data.model.Telemetry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TelemetryRepository(
    private val db  : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth      = FirebaseAuth.getInstance()
) {

    /** Devuelve la sub-colección de telemetría para una zona concreta del usuario. */
    private fun teleCol(zoneId: String) =
        db.collection("users")
            .document(auth.currentUser!!.uid)           // ← UID log-in
            .collection("zones")
            .document(zoneId)
            .collection("telemetry")

    /* ---------- escritura desde MQTT / app ---------- */
    suspend fun save(zoneId: String, reading: Telemetry) {
        teleCol(zoneId).add(reading).await()
    }

    /* ---------- última lectura de una zona (flow en vivo) ---------- */
    fun latestByZone(zoneId: String): Flow<Telemetry?> = callbackFlow {
        val reg = teleCol(zoneId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { qs, e ->
                if (e != null) {
                    close(e)
                } else {
                    trySend(
                        qs?.documents
                            ?.firstOrNull()
                            ?.toObject(Telemetry::class.java)
                    )
                }
            }
        awaitClose { reg.remove() }
    }
}
