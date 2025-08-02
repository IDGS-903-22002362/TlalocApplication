package com.lrosas.tlalocapplication.data

import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

object SensorRepository {

    /* --------------------- parámetros de calibración --------------------- */
    /** ADC con la sonda al aire (100 % seco)            */
    var VALOR_SECO   = 3400
    /** ADC sumergido / sustrato saturado (0 % seco)     */
    var VALOR_HUMEDO = 1700

    /* --------------------------- topic base ----------------------------- */
    private const val TOPIC_BASE = "tlaloc"

    /* -------------------- flujo crudo (topic → payload) ------------------ */
    private val raw: Flow<Pair<String, String>> =
        HiveMqManager.incoming
            .filter { (topic, _) -> topic.startsWith("$TOPIC_BASE/tele/") }

    /* -------------------- sensores individuales ------------------------- */

    /** Luxómetro (lx) */
    val luxFlow: Flow<Float> = raw
        .filter { (t, _) -> t.endsWith("/lux") }
        .map { (_, msg) -> msg.toFloatOrNull() ?: 0f }

    /** Humedad en % – regla de tres con valores de calibración */
    val humFlow: Flow<Int> = raw
        .filter { (t, _) -> t.endsWith("/humidity") }
        .map { (_, msg) ->
            val adc = msg.toIntOrNull() ?: 0
            val pct = (100f * (VALOR_SECO - adc) / (VALOR_SECO - VALOR_HUMEDO))
            pct.coerceIn(0f, 100f).toInt()
        }
    /* ------------- valor crudo de humedad (ADC 0-4095) ------------- */
    val humAdcFlow: Flow<Int> = raw
        // el ESP32 debe publicar  tlaloc/tele/zone1/humidityRaw   2840
        .filter { (t, _) -> t.endsWith("/humidityRaw") }
        .map { (_, msg) -> msg.toIntOrNull() ?: 0 }

    /** TDS / EC (ppm) */
    val tdsFlow: Flow<Float> = raw
        .filter { (t, _) -> t.endsWith("/tds") }
        .map { (_, msg) -> msg.toFloatOrNull() ?: 0f }

    /** Distancia (cm) del ultrasónico */
    val distFlow: Flow<Float> = raw
        .filter { (t, _) -> t.endsWith("/distance") }
        .map { (_, msg) -> msg.toFloatOrNull() ?: -1f }

    /* ------------------------ envío de comandos ------------------------- */

    /**
     * Envía un comando genérico:
     *  - zoneId  →  “zone1”, “zone2”…
     *  - key     →  “pump”, “led”, “stream” …
     *  - value   →  “ON”, “OFF”, “5” …
     */
    fun sendCommand(zoneId: String, key: String, value: String) =
        HiveMqManager.publishCmd(zoneId, key, value)

    /** Atajo específico para encender / apagar la bomba */
    fun setPump(zoneId: String, on: Boolean) =
        sendCommand(zoneId, "pump", if (on) "ON" else "OFF")

    /* -------------------- persistir nueva calibración ------------------- */

    suspend fun updateCalibration(seco: Int, humedo: Int) {
        VALOR_SECO   = seco
        VALOR_HUMEDO = humedo
        // TODO: persistir en DataStore si deseas que sobreviva a reinicios
    }
}