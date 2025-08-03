/*  data/repository/PlantRepository.kt  */
package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.lrosas.tlalocapplication.data.model.Care
import com.lrosas.tlalocapplication.data.model.Plant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlantRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /* ─────────── Colecciones de nivel raíz ─────────── */
    private val plantsCol get() = db.collection("plants")
    private val caresCol  get() = db.collection("cares")

    /* ─────────────────── PLANTS ─────────────────── */

    /** Flujo en vivo con **todas** las plantas. */
    fun getAllPlants(): Flow<List<Plant>> = callbackFlow {
        val sub: ListenerRegistration = plantsCol.addSnapshotListener { snap, err ->
            if (err != null) close(err)
            else snap?.let { trySend(it.toObjects(Plant::class.java)) }
        }
        awaitClose { sub.remove() }
    }

    /**
     * Devuelve la planta `plantId` (una sola vez).
     * Si no existe, devuelve `null`.
     */
    /* data/repository/PlantRepository.kt */
    suspend fun getPlant(id: String): Plant? =
        plantsCol.document(id).get().await().toObject(Plant::class.java)


    /**
     * Crea una planta con sólo el nombre y devuelve su **ID**.
     * Los demás campos de la UI (cuidados) se guardan en `cares`.
     */
    suspend fun addPlant(name: String): String =
        plantsCol.add(Plant(name = name)).await().id

    /**
     * Elimina por completo una planta y sus cuidados asociados.
     */
    suspend fun deletePlant(plantId: String) {
        plantsCol.document(plantId).delete().await()
        caresCol.document(plantId).delete().await()
    }

    /* ─────────────────── CARES ─────────────────── */

    /** Flujo en vivo con los cuidados de la planta. */
    fun getCareByPlant(plantId: String): Flow<Care?> = callbackFlow {
        val sub: ListenerRegistration =
            caresCol.document(plantId).addSnapshotListener { snap, err ->
                if (err != null) close(err)
                else trySend(snap?.toObject(Care::class.java))
            }
        awaitClose { sub.remove() }
    }

    /** Crea o reemplaza los cuidados de una planta (PUT completo). */
    suspend fun saveCare(care: Care) =
        caresCol.document(care.plantId).set(care).await()

    /**
     * Actualiza sólo los campos indicados (PATCH / merge).
     * Ejemplo: `updateCare("id123", mapOf("humidity" to 60))`
     */
    suspend fun updateCareFields(
        plantId: String,
        fields: Map<String, Any>
    ) = caresCol.document(plantId).update(fields).await()
}
