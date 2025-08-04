package com.lrosas.tlalocapplication.ui.zones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.model.Care
import com.lrosas.tlalocapplication.data.model.Telemetry
import com.lrosas.tlalocapplication.data.model.Zone
import com.lrosas.tlalocapplication.data.repository.CareRepository
import com.lrosas.tlalocapplication.data.repository.TelemetryRepository
import com.lrosas.tlalocapplication.data.repository.ZoneRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ZonesViewModel(
    private val zoneRepo : ZoneRepository      = ZoneRepository(),
    private val teleRepo : TelemetryRepository = TelemetryRepository(),
    private val careRepo : CareRepository      = CareRepository(),
    private val pumpDurationMs: Long            = 10_000L
) : ViewModel() {

    /* ─────────── Todas las zonas del usuario ─────────── */
    val zones: StateFlow<List<Zone>> =
        zoneRepo.getZones()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /* ─────────── Zonas + última lectura (para la grilla) ─────────── */
    val zonesWithReading: StateFlow<List<Pair<Zone, Telemetry?>>> =
        zones
            .flatMapLatest { list ->
                if (list.isEmpty()) flowOf(emptyList())
                else combine(
                    list.map { z -> teleRepo.live(z.id) }      // 👈 nuevo flujo
                ) { readings: Array<Telemetry?> ->
                    list.zip(readings.asList())
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /* ─────────── Zona seleccionada ─────────── */

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    /**
     *  Triple<Zone, Telemetry?, Care?> – o `null` si la zona ya no existe.
     */
    val selected: StateFlow<Triple<Zone, Telemetry?, Care?>?> =
        _selectedId
            .flatMapLatest { id ->
                if (id == null) flowOf(null)
                else zoneRepo.getZone(id).flatMapLatest { zone ->
                    if (zone == null) flowOf(null)
                    else combine(
                        teleRepo.live(zone.id),              // 👈 nuevo flujo
                        careRepo.getCareByPlant(zone.plantId)
                    ) { tele, care ->
                        Triple(zone, tele, care)
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /* ─────────── acciones de UI ─────────── */

    fun select(zoneId: String) {
        _selectedId.value = zoneId
    }

    fun renameZone(zoneId: String, newName: String) = viewModelScope.launch {
        zoneRepo.updateZoneName(zoneId, newName)
    }
    fun toggleAuto(zoneId: String, auto: Boolean) = viewModelScope.launch {
        zoneRepo.updateZoneFields(zoneId, mapOf("auto" to auto))
        HiveMqManager.publishCmd(zoneId, "auto", if (auto) "ON" else "OFF")
    }

    fun manualPump(zoneId: String) = viewModelScope.launch {
        HiveMqManager.publishCmd(zoneId, "pump", "ON")
        delay(pumpDurationMs)                         // espera
        HiveMqManager.publishCmd(zoneId, "pump", "OFF")
    }
    init {
        // ① En cuanto tengamos un care válido para la zona seleccionada…
        selected
            .filterNotNull()                    // solo cuando haya triple
            .mapNotNull { it.third }            // extrae el Care
            .map { care -> care.humidity }      // extrae el % humedad ideal
            .distinctUntilChanged()             // solo cuando cambie
            .onEach { idealPct ->
                // ② publica por MQTT el umbral al ESP32:
                HiveMqManager.publishCmd(
                    zone   = selectedId.value!!,          // ← aquí
                    action = "threshold",
                    value  = idealPct.toString()
                )
            }
            .launchIn(viewModelScope)           // ✨ arranca en el scope del VM
    }

}
