package com.lrosas.tlalocapplication.core.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.lrosas.tlalocapplication.data.model.Telemetry
import com.lrosas.tlalocapplication.data.repository.TelemetryRepository
import com.lrosas.tlalocapplication.data.store.BrokerCreds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

object HiveMqManager {

    private const val TAG = "HiveMqManager"

    /* ---------- credenciales por defecto (se sobre-escriben en overrideCreds) ---------- */
    private var HOST       = "68f2599da2244e8c85562ba450812c39.s1.eu.hivemq.cloud"
    private const val PORT = 8883
    private var USER       = "Betillo2"
    private var PASS       = "Uucy291o"
    private var TOPIC_BASE = "tlaloc"

    /* ---------- flujo crudo para depuración / consumo interno ---------- */
    private val _incoming = MutableSharedFlow<Pair<String, String>>(replay = 1)
    val incoming = _incoming.asSharedFlow()

    /* ---------- repositorio Firestore para persistir telemetría ---------- */
    private val telemetryRepo = TelemetryRepository()

    /* ---------- alcance para corrutinas internas ---------- */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /* ---------- cliente MQTT (mutable para reconstruir) ---------- */
    private var client: Mqtt3AsyncClient = buildClient()

    private fun buildClient(): Mqtt3AsyncClient =
        MqttClient.builder()
            .useMqttVersion3()
            .identifier("android-${UUID.randomUUID()}")
            .serverHost(HOST)
            .serverPort(PORT)
            .useSslWithDefaultConfig()
            .automaticReconnectWithDefaultConfig()
            .buildAsync()

    /* ────────────────────────── API pública ────────────────────────── */

    fun connect() {
        if (client.state.isConnected) return

        Log.d(TAG, "Conectando a $HOST …")
        client.connectWith()
            .simpleAuth()
            .username(USER)
            .password(PASS.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, err ->
                if (err != null) {
                    Log.e(TAG, "Error de conexión", err)
                } else {
                    Log.d(TAG, "✅ Conectado, suscribiendo telemetría")
                    subscribeToTelemetry()
                }
            }
    }

    fun publishCmd(zone: String, action: String, value: String = "") {
        val topic = "$TOPIC_BASE/cmd/$zone/$action"
        client.publishWith()
            .topic(topic)
            .payload(value.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
    }

    fun overrideCreds(c: BrokerCreds) {
        if (client.state.isConnected) client.disconnect()
        HOST       = c.host
        USER       = c.user
        PASS       = c.pass
        TOPIC_BASE = c.topic
        client     = buildClient()
    }

    /* ────────────────────────── privados ────────────────────────── */

    private fun subscribeToTelemetry() {
        /* registro global (solo debug) */
        client.publishes(MqttGlobalPublishFilter.ALL) { pub ->
            val payload = pub.payload.orElse(null)?.let { buf ->
                val arr = ByteArray(buf.remaining()); buf.get(arr); String(arr)
            } ?: return@publishes
            Log.v(TAG, "[ALL] ${pub.topic} → $payload")
        }

        /* suscripción útil */
        client.subscribeWith()
            .topicFilter("$TOPIC_BASE/tele/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { pub ->
                val bytes = pub.payload.orElse(null)?.let { buf ->
                    val arr = ByteArray(buf.remaining()); buf.get(arr); arr
                } ?: return@callback
                val msg = String(bytes, Charsets.UTF_8)

                // 1. Emitimos para flows internos
                _incoming.tryEmit(pub.topic.toString() to msg)

                // 2. Parseamos <root>/tele/<zoneId>/<sensor>
                val parts = pub.topic.toString().split("/")
                if (parts.size != 4) return@callback
                val zoneId = parts[2]
                val key    = parts[3]
                val value  = msg.toFloatOrNull() ?: return@callback

                // 3. Creamos lectura parcial
                val reading = Telemetry(
                    zoneId = zoneId,
                    humidity     = if (key == "humidity") value else null,
                    light        = if (key == "lux")      value else null,
                    waterLevel   = if (key == "distance") value else null,
                    waterQuality = if (key == "tds")      value else null
                )

                // 4. Guardamos en Firestore en background
                scope.launch {
                    telemetryRepo.save(reading.zoneId, reading)   // ✅ firma nueva
                }
            }
            .send()
    }
}
