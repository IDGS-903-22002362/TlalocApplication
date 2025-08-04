/*  ui/zones/ZoneDetailScreen.kt  */
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDetailScreen(
    zoneId : String,
    zonesVm: ZonesViewModel,         // ← llega desde NavHost
    onBack : () -> Unit
) {
    /* ① – si entramos “en frío” pedimos la selección al VM */
    LaunchedEffect(zoneId) {
        if (zonesVm.selectedId.value != zoneId) zonesVm.select(zoneId)
    }

    /* ② – triple en vivo (Zone · Telemetry? · Care?) */
    val triple by zonesVm.selected.collectAsStateWithLifecycle(initialValue = null)

    /* ③ – estados locales */
    var pumping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
    ) { pad ->

        if (triple == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        /* ------------ desempaquetado ------------ */
        val zone    = triple!!.first
        val reading = triple!!.second          // puede ser null
        val care    = triple!!.third           // puede ser null

        var auto by remember(zone) { mutableStateOf(zone.auto) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {

            /* ---------- Switch Automático ---------- */
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
                        zonesVm.toggleAuto(zone.id, checked)      // 🔄 Firestore + MQTT
                    }
                )
            }

            /* ---------- Botón riego manual ---------- */
            if (!auto) {
                Button(
                    enabled = !pumping,
                    onClick = {
                        pumping = true
                        zonesVm.manualPump(zone.id)               // ON → delay → OFF
                        scope.launch {
                            delay(10_000)                         // mismo tiempo que en VM
                            pumping = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (pumping) "Regando…" else "Regar ahora")
                }
            }

            /* ---------- Gauge de humedad ---------- */
            ElevatedCard {
                Box(
                    modifier = Modifier
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
                    Text(
                        "$pct %",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            /* ---------- Métricas ---------- */
            InfoRow("Luz",     reading?.light        ?.let { "%.0f lx".format(it) } ?: "--")
            InfoRow("Humedad", reading?.humidity     ?.let { "$it %" }             ?: "--")
            InfoRow("TDS",     reading?.waterQuality ?.let { "%.0f ppm".format(it) }?: "--")
            InfoRow("Nivel",   reading?.waterLevel   ?.let { "%.1f cm".format(it) } ?: "--")

            /* ---------- Umbral ideal (si existe) ---------- */
            care?.let {
                Text(
                    "Humedad ideal: ${it.humidity} %",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/* ---------- Helper visual ---------- */
@Composable
private fun InfoRow(label: String, value: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
