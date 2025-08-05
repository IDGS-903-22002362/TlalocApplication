package com.lrosas.tlalocapplication.ui.zones

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDetailScreen(
    zoneId: String,
    zonesVm: ZonesViewModel,
    onBack: () -> Unit,
    onHistory: () -> Unit
) {
    LaunchedEffect(zoneId) {
        if (zonesVm.selectedId.value != zoneId) zonesVm.select(zoneId)
    }

    val triple by zonesVm.selected.collectAsStateWithLifecycle(initialValue = null)
    var pumping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

    // Dimensiones físicas del tanque
    val tankHeightCm = 13f
    val tankRadiusCm = 9f / 2f

    Scaffold(
        containerColor = greenPrimary,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        triple?.first?.name ?: "Zona",
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
        if (triple == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = white)
            }
            return@Scaffold
        }

        val zone = triple!!.first
        val reading = triple!!.second
        val care = triple!!.third
        var auto by remember(zone) { mutableStateOf(zone.auto) }

        // Cálculo volumen actual
        val distCm = reading?.waterLevel ?: 0f
        val levelCm = (tankHeightCm - distCm).coerceIn(0f, tankHeightCm)
        val volumeL = (PI * tankRadiusCm * tankRadiusCm * levelCm) / 1000f

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Switch automático
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = white)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Modo automático",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = auto,
                        onCheckedChange = {
                            auto = it
                            zonesVm.toggleAuto(zone.id, it)
                        }
                    )
                }
            }

            // Ver historial
            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = white
                )
            ) {
                Text("Ver historial")
            }

            // Riego manual
            if (!auto) {
                Button(
                    onClick = {
                        pumping = true
                        zonesVm.manualPump(zone.id)
                        scope.launch {
                            delay(10_000)
                            pumping = false
                        }
                    },
                    enabled = !pumping,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = white,
                        contentColor = greenPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (pumping) "Regando…" else "Regar ahora")
                }
            }

            // Indicador de humedad
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = white)) {
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
                        modifier = Modifier.fillMaxSize(),
                        color = greenPrimary
                    )
                    Text(
                        "$pct %",
                        style = MaterialTheme.typography.headlineMedium,
                        color = greenPrimary
                    )
                }
            }

            // Métricas
            InfoRow("Luz", reading?.light?.let { "%.0f lx".format(it) } ?: "--", white)

            // Humedad crítica
            val humidity = reading?.humidity
            val idealHumidity = care?.humidity
            val humidityCritical =
                humidity != null && idealHumidity != null && humidity < (idealHumidity * 0.25)

            InfoRow(
                label = "Humedad",
                value = humidity?.let { "$it %" } ?: "--",
                background = white,
                warning = if (humidityCritical) "🌱 Riega tu planta" else null
            )

            InfoRow("TDS", reading?.waterQuality?.let { "%.0f ppm".format(it) } ?: "--", white)
            InfoRow("Nivel", "%.1f cm".format(levelCm), white)

            // Volumen crítico
            val volumeCritical = volumeL < 0.250
            InfoRow(
                label = "Volumen",
                value = "%.2f L".format(volumeL),
                isCritical = volumeCritical,
                warning = if (volumeCritical) "🫗 Rellena el depósito" else null,
                background = white
            )

            care?.let {
                Text(
                    "Humedad ideal: ${it.humidity} %",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    background: Color,
    isCritical: Boolean = false,
    warning: String? = null
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCritical) MaterialTheme.colorScheme.errorContainer else background
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
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
