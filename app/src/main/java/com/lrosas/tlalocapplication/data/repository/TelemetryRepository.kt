package com.lrosas.tlalocapplication.data.repository

/* ---------- Firebase ---------- */
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration

/* ---------- Domain ---------- */
import com.lrosas.tlalocapplication.data.model.Telemetry

/* ---------- Coroutines / Flow ---------- */
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TelemetryRepository(
    private val db   : FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth : FirebaseAuth      = FirebaseAuth.getInstance()
) {

    /** Ruta a `/users/{uid}/zones/{zoneId}/telemetry/last` */
    private fun lastDoc(zoneId: String) =
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("zones")
            .document(zoneId)
            .collection("telemetry")
            .document("last")

    /**
     * Guarda (merge) sólo los campos presentes en `fields`.
     * Si no incluye `"timestamp"`, lo añade como serverTimestamp().
     *
     * Ejemplos de uso:
     * ```
     * save("zone1", mapOf("humidity" to 45f))
     * save("zone1", mapOf("light" to 12000f, "waterLevel" to 2.3f))
     * ```
     */
    suspend fun save(zoneId: String, fields: Map<String, Any>) {
        val toWrite = if ("timestamp" in fields) {
            fields
        } else {
            fields + ("timestamp" to FieldValue.serverTimestamp())
        }

        lastDoc(zoneId)
            .set(toWrite, SetOptions.merge())
            .await()
    }

    /**
     * Flujo en vivo del documento único `last`.
     * Emitirá `null` si aún no existe.
     */
    fun live(zoneId: String): Flow<Telemetry?> = callbackFlow {
        val registration: ListenerRegistration = lastDoc(zoneId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.toObject(Telemetry::class.java))
                }
            }

        awaitClose { registration.remove() }
    }
}
