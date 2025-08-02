package com.lrosas.tlalocapplication.data

import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

object SensorRepository {

    /* --------------------- parámetros de calibración --------------------- */
    /** ADC con la sonda al aire (100 % seco).            */
    var VALOR_SECO   = 3400
    /** ADC sumergido / sustrato saturado (0 % seco).     */
    var VALOR_HUMEDO = 1700

    /* --------------------------- topic base ----------------------------- */
    private const val TOPIC_BASE = "tlaloc"

    /* ------------------ depuración: log de cada mensaje ----------------- */
    init {
        // Lanza una corrutina en segundo plano que imprime todo lo que llega
        HiveMqManager.incoming
            .onEach { (topic, msg) ->
                println("DEBUG-MQTT ➜ $topic -> $msg")
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
    }

    /* -------------------- flujo crudo (topic → payload) ------------------ */
    private val raw: Flow<Pair<String, String>> =
        HiveMqManager.incoming
            .filter { (topic, _) -> topic.startsWith("$TOPIC_BASE/tele/") }

    /* -------------------- sensores individuales ------------------------- */

    /** Luxómetro (lx) */
    val luxFlow: Flow<Float> = raw
        .filter { it.first.endsWith("/lux") }
        .map   { (_, msg) -> msg.toFloatOrNull() ?: 0f }

    /* --- humedad --- */
    val humAdcFlow: Flow<Int> = raw
        .filter { it.first.endsWith("/humidity") }
        .map   { (_, msg) -> msg.toIntOrNull() ?: 0 }

    /** Humedad en % (0-100) a partir del ADC crudo y la calibración. */
    val humFlow: Flow<Int> = humAdcFlow
        .map { adc ->
            val pct = 100f * (VALOR_SECO - adc) / (VALOR_SECO - VALOR_HUMEDO)
            pct.coerceIn(0f, 100f).toInt()
        }

    /** TDS / EC (ppm) */
    val tdsFlow: Flow<Float> = raw
        .filter { it.first.endsWith("/tds") }
        .map   { (_, msg) -> msg.toFloatOrNull() ?: 0f }

    /** Distancia (cm) medida por el ultrasónico */
    val distFlow: Flow<Float> = raw
        .filter { it.first.endsWith("/distance") }
        .map   { (_, msg) -> msg.toFloatOrNull() ?: -1f }

    /* ------------------------ envío de comandos ------------------------- */

    /**
     * Envía un comando genérico:
     *   zoneId → “zone1”, “zone2”…
     *   key    → “pump”, “led”, “stream”…
     *   value  → “ON”, “OFF”, “5”…
     */
    fun sendCommand(zoneId: String, key: String, value: String) =
        HiveMqManager.publishCmd(zoneId, key, value)

    /** Atajo para encender / apagar la bomba */
    fun setPump(zoneId: String, on: Boolean) =
        sendCommand(zoneId, "pump", if (on) "ON" else "OFF")

    /* -------------------- persistir nueva calibración ------------------- */

    suspend fun updateCalibration(seco: Int, humedo: Int) {
        VALOR_SECO   = seco
        VALOR_HUMEDO = humedo
        // TODO: guardar en DataStore si quieres conservar tras reinicios
    }
}