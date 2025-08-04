package com.lrosas.tlalocapplication.ui.zones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lrosas.tlalocapplication.data.model.Care
import com.lrosas.tlalocapplication.data.model.Telemetry
import com.lrosas.tlalocapplication.data.model.Zone
import com.lrosas.tlalocapplication.data.repository.CareRepository
import com.lrosas.tlalocapplication.data.repository.TelemetryRepository
import com.lrosas.tlalocapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ZonesViewModel(
    private val zoneRepo : ZoneRepository      = ZoneRepository(),
    private val teleRepo : TelemetryRepository = TelemetryRepository(),
    private val careRepo : CareRepository      = CareRepository()
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
                        careRepo.getCareByPlant(zone.id)
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
}
