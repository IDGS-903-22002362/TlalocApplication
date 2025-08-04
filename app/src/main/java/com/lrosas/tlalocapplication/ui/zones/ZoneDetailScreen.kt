/* ui/zones/ZoneDetailScreen.kt */

package com.lrosas.tlalocapplication.ui.zones

/* ---------- Compose ---------- */
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDetailScreen(
    zoneId   : String,
    zonesVm  : ZonesViewModel,         // VM compartido
    onBack   : () -> Unit,             // acción Atrás
    onHistory: () -> Unit              // acción Ver historial
) {
    // ① Selección “en frío”
    LaunchedEffect(zoneId) {
        if (zonesVm.selectedId.value != zoneId) {
            zonesVm.select(zoneId)
        }
    }

    // ② Observamos Zone · Telemetry? · Care?
    val triple by zonesVm.selected.collectAsStateWithLifecycle(initialValue = null)

    // Estados locales
    var pumping by remember { mutableStateOf(false) }
    var auto    by remember(triple?.first) { mutableStateOf(triple?.first?.auto ?: false) }
    val coroutineScope = rememberCoroutineScope()

    // Dimensiones del cilindro (cm)
    val tankHeightCm = 13f
    val diameterCm = 9f
    val tankRadiusCm = diameterCm / 2f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(triple?.first?.name ?: "Zona") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (triple == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Desempaquetamos
        val (zone, reading, care) = triple!!

        // Calculamos nivel y volumen
        val distCm   = reading?.waterLevel ?: 0f
        val levelCm  = (tankHeightCm - distCm).coerceIn(0f, tankHeightCm)
        val volumeCm3 = PI * tankRadiusCm * tankRadiusCm * levelCm
        val volumeL   = volumeCm3 / 1000f

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            // ─── Switch Automático ─────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Automático", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = auto,
                    onCheckedChange = { checked ->
                        auto = checked
                        zonesVm.toggleAuto(zone.id, checked)
                    }
                )
            }

            // ─── Botón Ver historial ───────────────────────────
            Button(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver historial")
            }

            // ─── Botón riego manual (solo si auto está OFF) ────
            if (!auto) {
                Button(
                    enabled = !pumping,
                    onClick = {
                        pumping = true
                        zonesVm.manualPump(zone.id)
                        coroutineScope.launch {
                            delay(10_000)
                            pumping = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (pumping) "Regando…" else "Regar ahora")
                }
            }

            // ─── Gauge de humedad ──────────────────────────────
            ElevatedCard {
                Box(
                    Modifier
                        .size(180.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val pct = reading?.humidity ?: 0
                    CircularProgressIndicator(
                        progress = pct.toFloat() / 100f,
                        strokeWidth = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text("$pct %", style = MaterialTheme.typography.headlineMedium)
                }
            }

            // ─── Métricas ──────────────────────────────────────
            // ─── Métricas ──────────────────────────────────────
            InfoRow("Luz", reading?.light?.let { "%.0f lx".format(it) } ?: "--")

// Humedad crítica
            val humidity = reading?.humidity
            val idealHumidity = care?.humidity
            val humidityCritical = humidity != null && idealHumidity != null && humidity < (idealHumidity * 0.25)

            InfoRow(
                label = "Humedad",
                value = humidity?.let { "$it %" } ?: "--",
                isCritical = humidityCritical,
                warning = if (humidityCritical) "🌱 Riega tu planta" else null
            )

// TDS
            InfoRow("TDS", reading?.waterQuality?.let { "%.0f ppm".format(it) } ?: "--")

// Nivel
            InfoRow("Nivel agua", "%.1f cm".format(levelCm))

// Volumen crítico
            val volumeCritical = volumeL < 0.250

            InfoRow(
                label = "Volumen",
                value = "%.2f L".format(volumeL),
                isCritical = volumeCritical,
                warning = if (volumeCritical) "🫗 Rellena el depósito" else null
            )

            // ─── Umbral ideal (Care) ───────────────────────────
            care?.let {
                Text(
                    "Humedad ideal: ${it.humidity} %",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Componente auxiliar para las filas de métricas */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    isCritical: Boolean = false,
    warning: String? = null
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            warning?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
