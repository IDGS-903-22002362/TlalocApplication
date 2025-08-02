package com.lrosas.tlalocapplication.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/* ---------- DataStore de la app ---------- */
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/* ---------- Modelo ---------- */
data class BrokerCreds(
    val host: String,
    val user: String,
    val pass: String,
    val topic: String
)

/* ---------- API ---------- */
object UserPrefs {

    private val KEY_BROKER = stringPreferencesKey("broker")  // host;user;pass;topic

    /** Guarda las credenciales como un único string  */
    suspend fun saveBroker(
        ctx: Context,
        host: String,
        user: String,
        pass: String,
        topic: String
    ) = ctx.dataStore.edit { pref ->
        pref[KEY_BROKER] = listOf(host, user, pass, topic).joinToString(";")
    }

    /** Flujo reactivo (null si aún no se ha emparejado) */
    fun brokerFlow(ctx: Context): Flow<BrokerCreds?> =
        ctx.dataStore.data.map { pref ->
            pref[KEY_BROKER]?.split(";")?.takeIf { it.size == 4 }?.let {
                BrokerCreds(it[0], it[1], it[2], it[3])
            }
        }

    /** True si ya hay credenciales persistidas */
    suspend fun hasBroker(ctx: Context): Boolean =
        brokerFlow(ctx).first() != null
}
