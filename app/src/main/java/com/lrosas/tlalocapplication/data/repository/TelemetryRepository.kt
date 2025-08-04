/*  data/repository/TelemetryRepository.kt  */

package com.lrosas.tlalocapplication.data.repository

/* ---------- Firebase ---------- */
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp

/* ---------- Domain ---------- */
import com.lrosas.tlalocapplication.data.model.Telemetry

/* ---------- Coroutines / Flow ---------- */
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TelemetryRepository(
    private val db  : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth      = FirebaseAuth.getInstance()
) {

    /* ───────────────────────── helpers ───────────────────────── */

    /** Sub‐colección telemetry ➜ documento único `last`. */
    private fun lastDoc(zoneId: String) =
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("zones")
            .document(zoneId)
            .collection("telemetry")
            .document("last")

    /* ───────────────────────── escritura ───────────────────────── */

    /**
     * Guarda (merge) las claves presentes en `fields`.
     * Si el map **no** trae la clave `"timestamp"`, la añade con
     * `FieldValue.serverTimestamp()`.
     *
     * ```kotlin
     * save("zone1", mapOf("humidity" to 48f))
     * save("zone1", mapOf("light" to 5300f, "timestamp" to Timestamp.now()))
     * ```
     */
    suspend fun save(zoneId: String, fields: Map<String, Any>) {
        val data = if ("timestamp" in fields) {
            fields                                    // ya viene el campo
        } else {
            fields + ("timestamp" to FieldValue.serverTimestamp())
        }

        lastDoc(zoneId)
            .set(data, SetOptions.merge())            // merge incremental ✅
            .await()
    }

    /* ───────────────────────── lectura en vivo ───────────────────────── */

    /**
     * Flujo (StateFlow-like) con el documento `last`.
     * ⇒ emite **null** mientras aún no existe nada.
     */
    fun live(zoneId: String): Flow<Telemetry?> = callbackFlow {
        val reg: ListenerRegistration = lastDoc(zoneId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                } else {
                    trySend(snap?.toObject(Telemetry::class.java))
                }
            }
        awaitClose { reg.remove() }
    }
}
