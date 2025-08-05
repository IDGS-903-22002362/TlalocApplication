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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lrosas.tlalocapplication.R
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.SensorRepository
import com.lrosas.tlalocapplication.ui.nav.Route
import kotlinx.coroutines.launch

private const val TAG = "SensorTestScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorTestScreen(
    onNavigate: (String) -> Unit = {},
    onConfirm: () -> Unit
) {
    LaunchedEffect(Unit) { HiveMqManager.connect() }

    LaunchedEffect(Unit) {
        launch { SensorRepository.luxFlow.collect { Log.d(TAG, "💡 luxFlow → $it") } }
        launch { SensorRepository.humPctFlow.collect { Log.d(TAG, "💧 humPctFlow → $it") } }
        launch { SensorRepository.tdsFlow.collect { Log.d(TAG, "🔬 tdsFlow → $it") } }
        launch { SensorRepository.distFlow.collect { Log.d(TAG, "📏 distFlow → $it") } }
    }

    val lux      by SensorRepository.luxFlow.collectAsState(initial = 0f)
    val humPct   by SensorRepository.humPctFlow.collectAsState(initial = 0)
    val tds      by SensorRepository.tdsFlow.collectAsState(initial = 0f)
    val distance by SensorRepository.distFlow.collectAsState(initial = -1f)

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

    var bombaOn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = greenPrimary,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Test de sensores",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = white
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Route.Calibrate.r) },
                containerColor = white,
                contentColor = greenPrimary
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Calibrar humedad")
            }
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = white)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DataRow("Luxómetro",     "%.0f lx".format(lux))
                    DataRow("Humedad suelo", "$humPct %")
                    DataRow("TDS",           "%.0f ppm".format(tds))
                    if (distance >= 0f) DataRow("Nivel agua", "%.1f cm".format(distance))
                }
            }

            Button(
                onClick = {
                    bombaOn = !bombaOn
                    scope.launch { SensorRepository.setPump("zone1", bombaOn) }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = white,
                    contentColor = greenPrimary
                )
            ) { Text(if (bombaOn) "Apagar bomba" else "Encender bomba") }


            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = white,
                    contentColor = greenPrimary
                )
            ) { Text("Continuar") }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}