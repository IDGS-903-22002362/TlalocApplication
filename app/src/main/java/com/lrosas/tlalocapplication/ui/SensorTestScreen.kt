package com.lrosas.tlalocapplication.ui

/* ---------- Compose & lifecycle ---------- */
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color

/* ---------- Dominio ---------- */
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.SensorRepository
import com.lrosas.tlalocapplication.ui.nav.Route
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorTestScreen(
    onNavigate: (String) -> Unit = {}          // lo recibes desde AppNav
) {
    /* ─────────────── Conectar MQTT una sola vez ─────────────── */
    LaunchedEffect(Unit) { HiveMqManager.connect() }

    /* ─────────────── Lecturas en vivo ─────────────── */
    val lux      by SensorRepository.luxFlow     .collectAsStateWithLifecycle(0f)
    val humPct   by SensorRepository.humFlow     .collectAsStateWithLifecycle(0)
    val tds      by SensorRepository.tdsFlow     .collectAsStateWithLifecycle(0f)
    val distance by SensorRepository.distFlow    .collectAsStateWithLifecycle(-1f) // opcional
    val lastError by HiveMqManager.errors.collectAsStateWithLifecycle(null)
    /* ─────────────── Estado local (bomba) ─────────────── */
    var bombaOn by remember { mutableStateOf(false) }
    val scope   = rememberCoroutineScope()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Test de sensores") }) },

        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigate(Route.Calibrate.r) }) {
                Icon(Icons.Default.Tune, contentDescription = "Calibrar humedad")
            }
        }
    ) { paddings ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {

            /* ----------- Tarjeta de datos ----------- */
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors()
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DataRow(label = "Luxómetro",  value = "%.0f lx".format(lux))
                    DataRow(label = "Humedad suelo", value = "$humPct %")
                    DataRow(label = "TDS",       value = "%.0f ppm".format(tds))
                    if (distance >= 0)
                        DataRow(label = "Nivel agua", value = "%.1f cm".format(distance))
                }
            }

            /* ----------- Control de bomba ----------- */
            Button(
                onClick = {
                    bombaOn = !bombaOn
                    scope.launch { SensorRepository.setPump("zone1", bombaOn) }
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(if (bombaOn) "Apagar bomba" else "Encender bomba")
            }

            /* ----------- Historial ----------- */
            OutlinedButton(
                onClick  = { onNavigate(Route.History.r) },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver historial")
            }
            lastError?.let { e ->                                    // ★ nuevo
                Text(
                    text = e.localizedMessage ?: "Error desconocido",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/* ─────────── Pequeño helper para mostrar pares etiqueta-valor ─────────── */
@Composable
private fun DataRow(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.displaySmall)
    }
}
