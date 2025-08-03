package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lrosas.tlalocapplication.data.model.Plant
import com.lrosas.tlalocapplication.data.model.Care
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlantRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val plantsCol get() = db.collection("plants")
    private val caresCol  get() = db.collection("cares")

    /* ----------- Plants ----------- */
    fun getAllPlants(): Flow<List<Plant>> = callbackFlow {
        val sub = plantsCol.addSnapshotListener { s, e ->
            if (e != null) close(e) else trySend(s!!.toObjects(Plant::class.java))
        }
        awaitClose { sub.remove() }
    }

    suspend fun addPlant(name: String): String =
        plantsCol.add(Plant(name = name)).await().id

    /* ----------- Cares ----------- */
    fun getCareByPlant(plantId: String): Flow<Care?> = callbackFlow {
        val sub = caresCol.document(plantId).addSnapshotListener { s, e ->
            if (e != null) close(e) else trySend(s?.toObject(Care::class.java))
        }
        awaitClose { sub.remove() }
    }

    suspend fun saveCare(care: Care) =
        caresCol.document(care.plantId).set(care).await()
}
