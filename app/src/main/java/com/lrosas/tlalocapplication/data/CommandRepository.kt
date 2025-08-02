package com.lrosas.tlalocapplication.data

import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager


object CommandRepository {

    fun pump(zone: String, on: Boolean) =
        HiveMqManager.publishCmd(zone, "pump", if (on) "ON" else "OFF")

    fun requestSnapshot(zone: String) =
        HiveMqManager.publishCmd(zone, "snapshot")
}