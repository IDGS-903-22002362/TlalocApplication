package com.lrosas.tlalocapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lrosas.tlalocapplication.data.model.Zone
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ZoneRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun zonesCol() = db.collection("users")
        .document(auth.currentUser!!.uid)
        .collection("zones")

    fun getZones(): Flow<List<Zone>> = callbackFlow {
        val sub = zonesCol().addSnapshotListener { s, e ->
            if (e != null) close(e) else trySend(s!!.toObjects(Zone::class.java))
        }
        awaitClose { sub.remove() }
    }

    suspend fun addZone(zone: Zone) {
        zonesCol().add(zone).await()
    }
}
