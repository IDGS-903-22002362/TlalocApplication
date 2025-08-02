package com.lrosas.tlalocapplication.data.store

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefs {
    private val KEY_HOST   = stringPreferencesKey("host")
    private val KEY_USER   = stringPreferencesKey("user")
    private val KEY_PASS   = stringPreferencesKey("pass")
    private val KEY_TOPIC  = stringPreferencesKey("topic_root")

    suspend fun saveBroker(ctx: Context, host: String, user: String, pass: String, topic: String) =
        ctx.dataStore.edit {
            it[KEY_HOST]  = host
            it[KEY_USER]  = user
            it[KEY_PASS]  = pass
            it[KEY_TOPIC] = topic
        }

    fun brokerFlow(ctx: Context): Flow<BrokerCreds?> =
        ctx.dataStore.data.map { p ->
            val h = p[KEY_HOST] ?: return@map null
            BrokerCreds(h, p[KEY_USER]!!, p[KEY_PASS]!!, p[KEY_TOPIC]!!)
        }
}

data class BrokerCreds(val host: String, val user: String, val pass: String, val topic: String)