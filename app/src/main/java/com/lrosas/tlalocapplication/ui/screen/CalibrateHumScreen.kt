package com.lrosas.tlalocapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lrosas.tlalocapplication.R
import com.lrosas.tlalocapplication.data.SensorRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrateHumScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val adc by SensorRepository.humAdcFlow
        .collectAsStateWithLifecycle(initialValue = SensorRepository.VALOR_HUMEDO)
    val humPct by SensorRepository.humPctFlow
        .collectAsStateWithLifecycle(initialValue = 0)

    var seco by remember { mutableStateOf<Int?>(null) }
    var humedo by remember { mutableStateOf<Int?>(null) }

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

    Scaffold(
        containerColor = greenPrimary,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Calibrar humedad",
                        color = white,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = white)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de lectura actual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = white)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Lectura ADC actual: $adc", style = MaterialTheme.typography.bodyMedium)
                    Text("Humedad estimada: $humPct %", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Botones de calibración
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { seco = adc },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = white,
                        contentColor = greenPrimary
                    )
                ) {
                    Text("Guardar SECO")
                }
                Button(
                    onClick = { humedo = adc },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = white,
                        contentColor = greenPrimary
                    )
                ) {
                    Text("Guardar HÚMEDO")
                }
            }

            // Resumen
            Text(
                "Seco = ${seco ?: "--"}   |   Húmedo = ${humedo ?: "--"}",
                style = MaterialTheme.typography.bodyMedium,
                color = white
            )

            Spacer(Modifier.weight(1f))

            // Botón aplicar
            Button(
                onClick = {
                    scope.launch {
                        SensorRepository.updateCalibration(
                            seco ?: adc,
                            humedo ?: adc
                        )
                        onBack()
                    }
                },
                enabled = (seco != null && humedo != null),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = white,
                    contentColor = greenPrimary
                )
            ) {
                Text("Aplicar y volver")
            }
        }
    }
}
