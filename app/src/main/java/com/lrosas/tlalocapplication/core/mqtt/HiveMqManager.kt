// file: core/mqtt/HiveMqManager.kt
package com.lrosas.tlalocapplication.core.mqtt

/* ---------- Android / Kotlin ---------- */
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/* ---------- HiveMQ ------------ */
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient

/* ---------- Firebase repos ---------- */
import com.lrosas.tlalocapplication.data.model.Telemetry
import com.lrosas.tlalocapplication.data.repository.HistoryRepository
import com.lrosas.tlalocapplication.data.repository.TelemetryRepository

/* ---------- Broker credentials ---------- */
import com.lrosas.tlalocapplication.data.store.BrokerCreds

/* ---------- Util ---------- */
import java.util.UUID

object HiveMqManager {

    private const val TAG = "HiveMqManager"

    /* --------------- credenciales por defecto --------------- */
    private var HOST       = "68f2599da2244e8c85562ba450812c39.s1.eu.hivemq.cloud"
    private const val PORT = 8883
    private var USER       = "Betillo2"
    private var PASS       = "Uucy291o"
    private var TOPIC_BASE = "tlaloc"

    /* --------------- repositorios --------------- */
    private val telemetryRepo = TelemetryRepository()
    private val historyRepo   = HistoryRepository()

    /* --------------- flujo interno de mensajes --------------- */
    private val _incoming = MutableSharedFlow<Pair<String, String>>(replay = 1)
    val incoming          = _incoming.asSharedFlow()

    /* --------------- corrutinas internas --------------- */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /* --------------- cliente MQTT --------------- */
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

    /* ======================================================== */
    /* API pública                                              */
    /* ======================================================== */

    /** Conecta (o reconecta) y suscribe la telemetría. */
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
                    Log.e(TAG, "❌ Error de conexión", err)
                } else {
                    Log.d(TAG, "✅ MQTT conectado — suscribiendo telemetría")
                    subscribeToTelemetry()
                }
            }
    }

    /**
     * Publica un comando en tlaloc/cmd/{zone}/{action}.
     * Ejemplo: publishCmd("zone1", "pump", "ON")
     */
    fun publishCmd(zone: String, action: String, value: String = "") {
        val topic = "$TOPIC_BASE/cmd/$zone/$action"
        client.publishWith()
            .topic(topic)
            .payload(value.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
    }

    /**
     * Sobre-escribe las credenciales/configuración MQTT en caliente.
     */
    fun overrideCreds(c: BrokerCreds) {
        if (client.state.isConnected) client.disconnect()
        HOST       = c.host
        USER       = c.user
        PASS       = c.pass
        TOPIC_BASE = c.topic
        client     = buildClient()
    }

    /* ======================================================== */
    /* Suscripción y persistencia                               */
    /* ======================================================== */

    private fun subscribeToTelemetry() {
        // 1) Debug: logueo de todos los mensajes
        client.publishes(MqttGlobalPublishFilter.ALL) { pub ->
            val payload = pub.payload.orElse(null)?.let { buf ->
                val arr = ByteArray(buf.remaining()).also { buf.get(it) }
                String(arr)
            } ?: return@publishes
            Log.v(TAG, "[ALL] ${pub.topic} → $payload")
        }

        // Buffer temporal por zona para agrupar las 4 métricas
        val buffers = mutableMapOf<String, MutableMap<String, Float>>()

        // 2) Suscripción real a tlaloc/tele/#
        client.subscribeWith()
            .topicFilter("$TOPIC_BASE/tele/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { pub ->
                // Extraemos topic y payload
                val topicStr = pub.topic.toString()
                val payload  = pub.payload.orElse(null)?.let { buf ->
                    val arr = ByteArray(buf.remaining()).also { buf.get(it) }
                    String(arr)
                } ?: return@callback

                // Emitimos a flujo interno
                _incoming.tryEmit(topicStr to payload)

                // Parseamos ruta: tlaloc/tele/{zoneId}/{sensor}
                val parts = topicStr.split("/")
                if (parts.size != 4 || parts[0] != TOPIC_BASE || parts[1] != "tele") return@callback
                val zoneId = parts[2]
                val key    = parts[3]
                val value  = payload.toFloatOrNull() ?: return@callback

                // 3) Actualizamos “último valor” en Firestore
                val partial = when (key) {
                    "humidity" -> mapOf("humidity" to value)
                    "lux"      -> mapOf("light"    to value)
                    "distance" -> mapOf("waterLevel"   to value)
                    "tds"      -> mapOf("waterQuality" to value)
                    else       -> return@callback
                }
                scope.launch {
                    telemetryRepo.save(zoneId, partial)
                }

                // 4) Acumulamos en buffer para histórico
                val buf = buffers.getOrPut(zoneId) { mutableMapOf() }
                buf[key] = value

                // Si ya tenemos las 4 métricas, guardamos histórico y reiniciamos
                if (buf.size == 4) {
                    val reading = Telemetry(
                        zoneId       = zoneId,
                        humidity     = buf["humidity"],
                        light        = buf["lux"],
                        waterLevel   = buf["distance"],
                        waterQuality = buf["tds"],
                        timestamp    = null // Timestamp lo genera Firestore server
                    )
                    scope.launch {
                        historyRepo.add(zoneId, reading)
                    }
                    buf.clear()
                }
            }
            .send()
    }
}
