package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.lrosas.tlalocapplication.data.model.Care
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Colección global: cares/{plantId}
 * Cada documento usa como ID el mismo plantId.
 */
class CareRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val caresRef get() = db.collection("cares")

    /** Flujo en tiempo real de los cuidados de una planta (nullable). */
    fun getCareByPlant(plantId: String): Flow<Care?> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = caresRef.document(plantId)
                .addSnapshotListener { snap, _ ->
                    trySend(snap?.toObject(Care::class.java))
                }
        } catch (e: Exception) {
            close(e)
        }
        awaitClose { registration?.remove() }
    }

    /** Crea o reemplaza los cuidados. */
    suspend fun saveCare(care: Care) {
        caresRef.document(care.plantId).set(care).await()
    }

    /** Actualiza sólo los campos indicados (merge). */
    suspend fun updateCare(plantId: String, fields: Map<String, Any>) {
        caresRef.document(plantId).update(fields).await()
    }

    /** Borra por completo los cuidados de una planta (opcional). */
    suspend fun deleteCare(plantId: String) {
        caresRef.document(plantId).delete().await()
    }
}
