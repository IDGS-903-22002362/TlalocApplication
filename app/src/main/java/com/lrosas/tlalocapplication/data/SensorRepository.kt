package com.lrosas.tlalocapplication.data

import android.util.Log
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object SensorRepository {
    private const val TAG        = "SensorRepository"
    private const val TOPIC_BASE = "tlaloc"

    var VALOR_SECO   = 3400
    var VALOR_HUMEDO = 1700

    // Scope para los stateIn
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Arranca la conexión una sola vez
        repoScope.launch { HiveMqManager.connect() }
    }

    /** Flujo “crudo” sin shareIn: retiene el último mensaje via replay=1 en incoming */
    private val raw: Flow<Pair<String, String>> =
        HiveMqManager.incoming
            .onEach { Log.v(TAG, "raw → $it") }
            .filter { it.first.startsWith("$TOPIC_BASE/tele/") }

    /** Luxómetro */
    val luxFlow: StateFlow<Float> = raw
        .filter { it.first.endsWith("/lux") }
        .map { it.second.toFloatOrNull() ?: 0f }
        .onEach { Log.d(TAG, "→ luxFlow: $it") }
        .stateIn(repoScope, SharingStarted.Eagerly, 0f)

    /** Humedad ADC crudo */
    val humAdcFlow: StateFlow<Int> = raw
        .filter { it.first.endsWith("/humidity") }
        .map { it.second.toIntOrNull() ?: VALOR_HUMEDO }
        .onEach { Log.d(TAG, "→ humAdcFlow: $it") }
        .stateIn(repoScope, SharingStarted.Eagerly, VALOR_HUMEDO)

    /** Humedad % (directo del hardware) */
    val humPctFlow: StateFlow<Int> = raw
        .filter { it.first.endsWith("/humidity") }
        .map { it.second.toIntOrNull() ?: 0 }
        .onEach { Log.d(TAG, "→ humPctFlow: $it") }
        .stateIn(repoScope, SharingStarted.Eagerly, 0)

    /** TDS / EC */
    val tdsFlow: StateFlow<Float> = raw
        .filter { it.first.endsWith("/tds") }
        .map { it.second.toFloatOrNull() ?: 0f }
        .onEach { Log.d(TAG, "→ tdsFlow: $it") }
        .stateIn(repoScope, SharingStarted.Eagerly, 0f)

    /** Distancia (cm) */
    val distFlow: StateFlow<Float> = raw
        .filter { it.first.endsWith("/distance") }
        .map { it.second.toFloatOrNull() ?: -1f }
        .onEach { Log.d(TAG, "→ distFlow: $it") }
        .stateIn(repoScope, SharingStarted.Eagerly, -1f)

    /** Guarda calibración de seco/húmedo */
    suspend fun updateCalibration(seco: Int, humedo: Int) {
        Log.d(TAG, "Calibración guardada: seco=$seco, humedo=$humedo")
        VALOR_SECO   = seco
        VALOR_HUMEDO = humedo
    }

    /** Control de bomba */
    fun setPump(zoneId: String, on: Boolean) {
        repoScope.launch {
            HiveMqManager.publishCmd(zoneId, "pump", if (on) "ON" else "OFF")
        }
    }
}
