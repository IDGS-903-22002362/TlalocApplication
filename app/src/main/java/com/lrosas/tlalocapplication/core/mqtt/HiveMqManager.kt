package com.lrosas.tlalocapplication.core.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.lrosas.tlalocapplication.data.store.BrokerCreds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

object HiveMqManager {

    private const val TAG = "HiveMqManager"

    // 1) Primero definimos las credenciales/config:
    private var HOST       = "68f2599da2244e8c85562ba450812c39.s1.eu.hivemq.cloud"
    private const val PORT = 8883
    private var USER       = "Betillo2"
    private var PASS       = "Uucy291o"
    private var TOPIC_BASE = "tlaloc"

    // 2) Flujo de incoming con replay=1 para retener el último mensaje
    private val _incoming = MutableSharedFlow<Pair<String, String>>(replay = 1)
    val incoming = _incoming.asSharedFlow()

    // 3) Cliente MQTT, construido una vez con los valores de arriba.
    //    Lo hacemos `var` para poder recomponerlo en overrideCreds().
    private var client: Mqtt3AsyncClient = buildClient()

    private fun buildClient(): Mqtt3AsyncClient {
        Log.d(TAG, "🔨 buildClient() usando HOST=$HOST")
        return MqttClient.builder()
            .useMqttVersion3()
            .identifier("android-${UUID.randomUUID()}")
            .serverHost(HOST)
            .serverPort(PORT)
            .useSslWithDefaultConfig()
            .automaticReconnectWithDefaultConfig()
            .buildAsync()
    }

    /** Conecta (TLS) y se suscribe a telemetría */
    fun connect() {
        // Simplemente revisamos el estado conectado, no usamos isInitialized.
        if (client.state.isConnected) {
            Log.d(TAG, "Ya conectado; omitiendo connect()")
            return
        }
        Log.d(TAG, "Iniciando conexión MQTT…")
        client.connectWith()
            .simpleAuth()
            .username(USER)
            .password(PASS.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, err ->
                if (err != null) {
                    Log.e(TAG, "Error al conectar MQTT", err)
                } else {
                    Log.d(TAG, "✅ Conectado. Suscribiendo a $TOPIC_BASE/tele/#")
                    subscribeToTelemetry()
                }
            }
    }

    /** Publica un comando genérico …/cmd/{zone}/{action} */
    fun publishCmd(zone: String, action: String, value: String = "") {
        val topic = "$TOPIC_BASE/cmd/$zone/$action"
        Log.d(TAG, "📤 Publicando en $topic → $value")
        client.publishWith()
            .topic(topic)
            .payload(value.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
    }

    /** Override de credenciales: desconecta y reconstruye el cliente */
    fun overrideCreds(c: BrokerCreds) {
        Log.d(TAG, "Override credentials: $c")
        if (client.state.isConnected) {
            client.disconnect()
        }
        HOST       = c.host
        USER       = c.user
        PASS       = c.pass
        TOPIC_BASE = c.topic
        client     = buildClient()  // reconstruimos con la nueva HOST/USER/PASS/TOPIC
    }

    private fun subscribeToTelemetry() {
        client.subscribeWith()
            .topicFilter("$TOPIC_BASE/tele/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { pub ->
                val buf = pub.payload.orElse(null) ?: return@callback
                val bytes = ByteArray(buf.remaining()).also { buf.get(it) }
                val msg = String(bytes, Charsets.UTF_8)
                Log.d(TAG, "📥 Recibido topic=${pub.topic} payload=$msg")
                _incoming.tryEmit(pub.topic.toString() to msg)
            }
            .send()
            .whenComplete { _, err ->
                if (err != null) Log.e(TAG, "❌ Suscripción fallida", err)
                else            Log.d(TAG, "✅ Suscrito a $TOPIC_BASE/tele/#")
            }

        // (Opcional) listener global para debug
        client.publishes(MqttGlobalPublishFilter.ALL) { pub ->
            val buf = pub.payload.orElse(null) ?: return@publishes
            val bytes = ByteArray(buf.remaining()).also { buf.get(it) }
            val msg = String(bytes, Charsets.UTF_8)
            Log.v(TAG, "[ALL] topic=${pub.topic} → $msg")
        }
    }
}
