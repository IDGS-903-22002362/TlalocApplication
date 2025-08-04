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

/* ---------- Firebase ---------- */
import com.google.firebase.Timestamp
import com.lrosas.tlalocapplication.data.repository.TelemetryRepository
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

    /* --------------- flujo interno de mensajes --------------- */
    private val _incoming = MutableSharedFlow<Pair<String, String>>(replay = 1)
    val incoming          = _incoming.asSharedFlow()

    /* --------------- dependencias --------------- */
    private val telemetryRepo = TelemetryRepository()

    /* --------------- corrutinas internas --------------- */
    private val scope  = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    fun connect() {
        if (client.state.isConnected) return      // ya estaba conectado

        Log.d(TAG, "Conectando a $HOST …")
        client.connectWith()
            .simpleAuth()
            .username(USER)
            .password(PASS.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, err ->
                if (err != null) Log.e(TAG, "❌ Error de conexión", err)
                else {
                    Log.d(TAG, "✅ MQTT conectado — suscribiendo telemetría")
                    subscribeToTelemetry()
                }
            }
    }

    /** Publica un comando en *tlaloc/cmd/<zone>/<action>*  */
    fun publishCmd(zone: String, action: String, value: String = "") {
        client.publishWith()
            .topic("$TOPIC_BASE/cmd/$zone/$action")
            .payload(value.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
    }

    /** Re-configura host / user / pass / topic base en caliente. */
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

        /* 1 ▸ log global (debug) */
        client.publishes(MqttGlobalPublishFilter.ALL) { pub ->
            val payload = pub.payload.orElse(null)?.let { buf ->
                val arr = ByteArray(buf.remaining()); buf.get(arr); String(arr)
            } ?: return@publishes
            Log.v(TAG, "[ALL] ${pub.topic} → $payload")
        }

        /* 2 ▸ suscripción útil */
        client.subscribeWith()
            .topicFilter("$TOPIC_BASE/tele/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { pub ->

                /* ---- 2.1 extraer payload como String ---- */
                val msg = pub.payload.orElse(null)?.let { buf ->
                    val arr = ByteArray(buf.remaining()); buf.get(arr); String(arr)
                } ?: return@callback

                /* ---- 2.2 re-emitir crudo para quien lo necesite ---- */
                _incoming.tryEmit(pub.topic.toString() to msg)

                /* ---- 2.3 parsear ruta tlaloc/tele/<zoneId>/<sensor> ---- */
                val parts = pub.topic.toString().split("/")
                if (parts.size != 4) return@callback

                val zoneId = parts[2]
                val key    = parts[3]
                val value  = msg.toFloatOrNull() ?: return@callback

                /* ---- 3 ▸ mapa parcial + timestamp ---- */
                val fields = when (key) {
                    "humidity" -> mapOf("humidity"     to value)
                    "lux"      -> mapOf("light"        to value)
                    "distance" -> mapOf("waterLevel"   to value)
                    "tds"      -> mapOf("waterQuality" to value)
                    else       -> return@callback       // sensor desconocido
                } + mapOf("timestamp" to Timestamp.now())

                /* ---- 4 ▸ escritura asíncrona en Firestore ---- */
                scope.launch {
                    try {
                        telemetryRepo.save(zoneId, fields)          // 5
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error guardando telemetría", e)
                    }
                }
            }
            .send()
    }
}
