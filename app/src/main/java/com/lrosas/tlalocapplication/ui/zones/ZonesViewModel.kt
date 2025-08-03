/*  ui/zones/ZonesViewModel.kt  */
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
    private val zoneRepo: ZoneRepository = ZoneRepository(),
    private val teleRepo: TelemetryRepository = TelemetryRepository(),
    private val careRepo: CareRepository = CareRepository()
) : ViewModel() {

    /* -------- flujo con TODAS las zonas -------- */
    val zones: StateFlow<List<Zone>> = zoneRepo.getZones()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /* ---------- zona + última lectura (lista para la grilla) -------- */
    val zonesWithReading: StateFlow<List<Pair<Zone, Telemetry?>>> =
        zones
            .flatMapLatest { list ->
                if (list.isEmpty()) flowOf(emptyList())
                else combine(
                    list.map { z -> teleRepo.latestByZone(z.id) }
                ) { readings: Array<Telemetry?> ->
                    list.zip(readings)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /* ------------- estado de selección ------------- */

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    /**
     * Triple<Zone, Telemetry?, Care?> siempre que exista la zona.
     * Si la zona desaparece de Firestore → null.
     */
    val selected: StateFlow<Triple<Zone, Telemetry?, Care?>?> =
        combine(
            zones,
            _selectedId.filterNotNull(),
        ) { list, id -> list.find { it.id == id } }      // --> Zone?
            .distinctUntilChanged()
            .flatMapLatest { zoneOrNull ->
                if (zoneOrNull == null) flowOf(null)
                else combine(
                    teleRepo.latestByZone(zoneOrNull.id),
                    careRepo.getCareByPlant(zoneOrNull.id)
                ) { tele, care ->
                    Triple(zoneOrNull, tele, care)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /* -------------- acciones de UI -------------- */

    fun select(zoneId: String) {
        _selectedId.value = zoneId
    }

    /** Ejemplo de actualización (si añades updateZone al repositorio). */
    fun renameZone(zoneId: String, newName: String) = viewModelScope.launch {
        zoneRepo.updateZoneName(zoneId, newName)   // ← debes tener este helper
    }
}
