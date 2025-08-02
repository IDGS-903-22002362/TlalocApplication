package com.lrosas.tlalocapplication.core.mqtt


import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.lrosas.tlalocapplication.data.store.BrokerCreds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import java.util.UUID

object HiveMqManager {

    /* ---------- credenciales por defecto (se sobre-escriben vía overrideCreds) ---------- */
    private var HOST = "68f2599da2244e8c85562ba450812c39.s1.eu.hivemq.cloud"
    private const val PORT = 8883
    private var USER = "Betillo2"
    private var PASS = "Uucy291o"
    private var TOPIC_BASE = "tlaloc"

    /* ------------------------------- flujo de entrada ---------------------------------- */
    private val _incoming = MutableSharedFlow<Pair<String, String>>(replay = 0)
    val incoming = _incoming.asSharedFlow()            // topic → payload

    /* ----------------------------- construcción cliente -------------------------------- */
    private var client: Mqtt3AsyncClient = buildClient()

    private fun buildClient(): Mqtt3AsyncClient =
        MqttClient.builder()
            .useMqttVersion3()
            .identifier("android-${UUID.randomUUID()}")
            .serverHost(HOST)
            .serverPort(PORT)
            .useSslWithDefaultConfig()                 // TLS sin certs propios
            .buildAsync()

    /* ----------------------------------- público --------------------------------------- */

    /** Conecta (TLS) y se suscribe a telemetría */
    fun connect() {
        if (client.state.isConnected) return

        client.connectWith()
            .simpleAuth().username(USER).password(PASS.toByteArray()).applySimpleAuth()
            .send()
            .whenComplete { _, t ->
                if (t == null) subscribeToTelemetry()
            }
    }

    /** Reemplaza host/usuario/contraseña ↔ reconstruye cliente */
    fun overrideCreds(c: BrokerCreds) {
        if (client.state.isConnected) client.disconnect()
        HOST = c.host
        USER = c.user
        PASS = c.pass
        TOPIC_BASE = c.topic
        client = buildClient()
    }

    /** Publica un texto en el topic indicado (reconecta si fuera necesario) */
    fun publish(topic: String, msg: String) {
        if (!client.state.isConnected) connect()
        client.publishWith()
            .topic(topic)
            .payload(msg.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
    }

    /** Helper para mandar órdenes: .../cmd/{zone}/{action} */
    fun publishCmd(zone: String, action: String, value: String = "") =
        publish("$TOPIC_BASE/cmd/$zone/$action", value)
    /* ---------------------------------------------------------------------------------- */

    /* ---------------------------------- privados -------------------------------------- */
    private fun subscribeToTelemetry() {
        client.subscribeWith()
            .topicFilter("$TOPIC_BASE/tele/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()

        client.publishes(MqttGlobalPublishFilter.ALL) { pub: Mqtt3Publish ->
            val payload = pub.payload.map(ByteBuffer::remaining).orElse(0).let { len ->
                val bytes = ByteArray(len)
                pub.payload.get().get(bytes)
                String(bytes, Charsets.UTF_8)
            }
            _incoming.tryEmit(pub.topic.toString() to payload)
        }
    }
}