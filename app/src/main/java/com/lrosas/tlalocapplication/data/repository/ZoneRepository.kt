package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.lrosas.tlalocapplication.data.model.Zone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ZoneRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth     = FirebaseAuth.getInstance()
) {

    /* --------------------- referencia a sub-colección --------------------- */

    /** users/{uid}/zones */
    private fun zonesCol() = db.collection("users")
        .document(requireNotNull(auth.currentUser?.uid) { "Usuario no autenticado" })
        .collection("zones")

    /* -------------------------- lecturas -------------------------- */

    /** Flujo vivo con TODAS las zonas ordenadas por nombre. */
    fun getZones(): Flow<List<Zone>> =
        zonesCol()
            .orderBy("name")
            .snapshots()
            .map { qs ->
                qs.documents.mapNotNull { d ->
                    d.toObject(Zone::class.java)?.copy(id = d.id)
                }
            }

    /** Flujo con UNA zona concreta (o `null` si se borra). */
    fun getZone(id: String): Flow<Zone?> =
        zonesCol().document(id)
            .snapshots()
            .map { d -> d.toObject(Zone::class.java)?.copy(id = d.id) }

    /* -------------------- escritura / actualización -------------------- */

    /** Crea una zona y devuelve el `id` generado. */
    suspend fun addZone(zone: Zone): String {
        val ref = zonesCol().add(zone).await()
        return ref.id
    }

    /** Elimina una zona por su id. */
    suspend fun deleteZone(id: String) {
        zonesCol().document(id).delete().await()
    }

    /**
     * Actualiza únicamente el nombre de la zona.
     *  (Separado para que el ViewModel pueda llamarlo fácilmente.)
     */
    suspend fun updateZoneName(id: String, newName: String) {
        zonesCol().document(id).update("name", newName).await()
    }

    /**
     * Actualización genérica por campos (por si la necesitas en el futuro).
     *  Ej.:  updateZone("abc123", mapOf("plantId" to "plant42"))
     */
    suspend fun updateZone(id: String, fields: Map<String, Any>) {
        zonesCol().document(id).update(fields).await()
    }

    suspend fun addZoneWithId(id: String, zone: Zone) {
        zonesCol()              // …/users/{uid}/zones
            .document(id)       // 👉 usamos el id que pase la UI
            .set(zone)          // (PUT completo)
            .await()
    }
    suspend fun nextSequentialNumber(): Int {
        val snap = zonesCol().get().await()
        // Filtra ids "zoneN" y localiza el mayor N
        val max = snap.documents.mapNotNull { doc ->
            doc.id.removePrefix("zone").toIntOrNull()
        }.maxOrNull() ?: 0
        return max + 1
    }

}
