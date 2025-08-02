package com.lrosas.tlalocapplication.ui

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
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.SensorRepository
import com.lrosas.tlalocapplication.ui.nav.Route
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorTestScreen(
    onNavigate: (String) -> Unit = {}          // se lo pasas desde NavHost
) {
    /* ---------- Conexión MQTT una sola vez ---------- */
    LaunchedEffect(Unit) { HiveMqManager.connect() }

    /* ---------- Lecturas en vivo ---------- */
    val lux by SensorRepository.luxFlow.collectAsStateWithLifecycle(initialValue = 0f)
    val hum by SensorRepository.humFlow.collectAsStateWithLifecycle(initialValue = 0)
    val tds by SensorRepository.tdsFlow.collectAsStateWithLifecycle(initialValue = 0f)

    /* ---------- Estado de la bomba ---------- */
    var bombaOn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Test de sensores") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigate(Route.Calibrate.r) }) {
                Icon(Icons.Default.Tune, contentDescription = "Calibrar")
            }
        }
    ) { padd ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* ---------- Bloque de valores ---------- */
            Text("Luxómetro", style = MaterialTheme.typography.titleMedium)
            Text("%.0f lx".format(lux), style = MaterialTheme.typography.displaySmall)

            Text("Humedad suelo", style = MaterialTheme.typography.titleMedium)
            Text("$hum %", style = MaterialTheme.typography.displaySmall)

            Text("TDS / EC", style = MaterialTheme.typography.titleMedium)
            Text("%.0f ppm".format(tds), style = MaterialTheme.typography.displaySmall)

            /* ---------- Botón bomba ---------- */
            Button(
                onClick = {
                    bombaOn = !bombaOn
                    scope.launch {
                        SensorRepository.setPump("zone1", bombaOn)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(if (bombaOn) "Apagar bomba" else "Encender bomba")
            }

            /* ---------- Acceso a historial ---------- */
            OutlinedButton(
                onClick = { onNavigate(Route.History.r) },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver historial")
            }
        }
    }
}