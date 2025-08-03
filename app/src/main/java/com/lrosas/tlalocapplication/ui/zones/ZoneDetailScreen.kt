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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDetailScreen(
    zoneId : String,
    zonesVm: ZonesViewModel,           // 👈  único parámetro “extra” que llega del NavHost
    onBack : () -> Unit
) {
    /* ① – si entramos “en frío”, pide al VM que seleccione la zona */
    LaunchedEffect(zoneId) {
        if (zonesVm.selectedId.value != zoneId) zonesVm.select(zoneId)
    }

    /* ② – triple 〈Zone, Telemetry?, Care?〉 en vivo */
    val triple by zonesVm.selected.collectAsStateWithLifecycle(initialValue = null)

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
                Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val zone    = triple!!.first
        val reading = triple!!.second      // puede ser null
        val care    = triple!!.third       // puede ser null

        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            /* ---------- Gauge humedad ---------- */
            ElevatedCard {
                Box(
                    Modifier
                        .size(180.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val pct = reading?.humidity ?: 0
                    CircularProgressIndicator(
                        progress = pct.toFloat() / 100f,       // ← conversión a Float
                        strokeWidth = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text("$pct %", style = MaterialTheme.typography.headlineMedium)
                }
            }

            /* ---------- Métricas ---------- */
            InfoRow("Luz",       reading?.light        ?.let { "%.0f lx".format(it) } ?: "--")
            InfoRow("Humedad",   reading?.humidity     ?.let { "$it %" }             ?: "--")
            InfoRow("TDS",       reading?.waterQuality ?.let { "%.0f ppm".format(it) }?: "--")
            InfoRow("Nivel",     reading?.waterLevel   ?.let { "%.1f cm".format(it) } ?: "--")

            care?.let {
                Text("Humedad ideal: ${it.humidity} %", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
