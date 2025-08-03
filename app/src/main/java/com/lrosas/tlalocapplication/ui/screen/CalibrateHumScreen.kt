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

    // Lecturas en crudo (ADC) y porcentaje real de humedad
    val adc by SensorRepository.humAdcFlow
        .collectAsStateWithLifecycle(initialValue = SensorRepository.VALOR_HUMEDO)
    val humPct by SensorRepository.humPctFlow
        .collectAsStateWithLifecycle(initialValue = 0)

    // Valores que el usuario marca como “seco” y “húmedo”
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
            // Muestra la lectura actual
            Text("Lectura ADC actual: $adc")
            Text("Humedad estimada: $humPct %")

            // Botones para marcar seco y húmedo
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { seco = adc }) {
                    Text("Guardar SECO")
                }
                Button(onClick = { humedo = adc }) {
                    Text("Guardar HÚMEDO")
                }
            }

            // Resumen de marcadores
            Text(
                "Seco = ${seco ?: "--"}   |   Húmedo = ${humedo ?: "--"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            // Aplica la calibración y vuelve atrás
            Button(
                onClick = {
                    scope.launch {
                        // Si algún marcador fuera null, usamos la lectura actual por seguridad
                        SensorRepository.updateCalibration(
                            seco   ?: adc,
                            humedo ?: adc
                        )
                        onBack()
                    }
                },
                enabled = (seco != null && humedo != null),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar y volver")
            }
        }
    }
}
