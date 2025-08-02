package com.lrosas.tlalocapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lrosas.tlalocapplication.data.SensorRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrateHumScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // ← valores en vivo
    val adc by SensorRepository.humAdcFlow.collectAsStateWithLifecycle(0)
    val humPct by SensorRepository.humFlow.collectAsStateWithLifecycle(0)

    // ← marcadores que el usuario guarda
    var seco   by remember { mutableStateOf<Int?>(null) }
    var humedo by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calibrar humedad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Lectura ADC actual: $adc")
            Text("Humedad estimada: $humPct %")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { seco   = adc }) { Text("Guardar SECO") }
                Button(onClick = { humedo = adc }) { Text("Guardar HÚMEDO") }
            }

            Text("Seco = ${seco ?: "--"}   |   Húmedo = ${humedo ?: "--"}")

            Spacer(Modifier.height(24.dp))

            Button(
                enabled = seco != null && humedo != null,
                onClick = {
                    scope.launch {
                        SensorRepository.updateCalibration(seco!!, humedo!!)
                        onBack()
                    }
                }
            ) { Text("Aplicar y volver") }
        }
    }
}
