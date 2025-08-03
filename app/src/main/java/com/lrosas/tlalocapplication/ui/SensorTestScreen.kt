package com.lrosas.tlalocapplication.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.SensorRepository
import com.lrosas.tlalocapplication.ui.nav.Route
import kotlinx.coroutines.launch

private const val TAG = "SensorTestScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorTestScreen(
    onNavigate: (String) -> Unit = {}
) {
    // 1) Conecta MQTT solo una vez
    LaunchedEffect(Unit) {
        HiveMqManager.connect()
    }

    // 2) DEBUG: imprime cada emisión de cada flujo en Compose
    LaunchedEffect(Unit) {
        launch {
            SensorRepository.luxFlow.collect { Log.d(TAG, "💡 luxFlow → $it") }
        }
        launch {
            SensorRepository.humPctFlow.collect { Log.d(TAG, "💧 humPctFlow → $it") }
        }
        launch {
            SensorRepository.tdsFlow.collect { Log.d(TAG, "🔬 tdsFlow → $it") }
        }
        launch {
            SensorRepository.distFlow.collect { Log.d(TAG, "📏 distFlow → $it") }
        }
    }

    // 3) Lectura de StateFlows en UI
    val lux      by SensorRepository.luxFlow.collectAsState(initial = 0f)
    val humPct   by SensorRepository.humPctFlow.collectAsState(initial = 0)
    val tds      by SensorRepository.tdsFlow.collectAsState(initial = 0f)
    val distance by SensorRepository.distFlow.collectAsState(initial = -1f)

    // Estado local de la bomba
    var bombaOn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Test de sensores") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigate(Route.Calibrate.r) }) {
                Icon(Icons.Default.Tune, contentDescription = "Calibrar humedad")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DataRow("Luxómetro",     "%.0f lx".format(lux))
                    DataRow("Humedad suelo", "$humPct %")
                    DataRow("TDS",           "%.0f ppm".format(tds))
                    if (distance >= 0f) {
                        DataRow("Nivel agua", "%.1f cm".format(distance))
                    }
                }
            }

            Button(
                onClick = {
                    bombaOn = !bombaOn
                    scope.launch { SensorRepository.setPump("zone1", bombaOn) }
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(if (bombaOn) "Apagar bomba" else "Encender bomba")
            }

            OutlinedButton(
                onClick = { onNavigate(Route.History.r) },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.History, contentDescription = "Ver historial")
                Spacer(Modifier.width(8.dp))
                Text("Ver historial")
            }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.displaySmall)
    }
}
