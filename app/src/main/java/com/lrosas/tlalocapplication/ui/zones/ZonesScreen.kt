package com.lrosas.tlalocapplication.ui.zones

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lrosas.tlalocapplication.R
import com.lrosas.tlalocapplication.data.model.Zone

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ZonesScreen(
    onAdd: () -> Unit,
    onSelect: (String) -> Unit,
    vm: ZonesViewModel = viewModel()
) {
    val zones by vm.zonesWithHumidity.collectAsState(initial = emptyList())
    val zonesWithHumidity by vm.zonesWithHumidity.collectAsState()
    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)
    val redAlert = Color(0xFFD32F2F)

    Scaffold(
        containerColor = greenPrimary,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Inicio",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
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
                onClick = onAdd,
                containerColor = white,
                contentColor = greenPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar zona")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de alerta
            AlertCard(message = "El nivel del depósito es bajo")

            // Tarjetas de estado superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusCard(title = "Depósito 15%", value = "85%", icon = R.drawable.ic_water_drop)
                StatusCard(title = "Batería", value = "100% por cable", icon = R.drawable.ic_battery)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cuadrícula de zonas
            if (zones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onAdd,
                        containerColor = white,
                        contentColor = greenPrimary
                    ) {
                        Text("Añadir zona")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(greenPrimary)
                ) {
                    items(zonesWithHumidity.size) { idx ->
                        val item = zonesWithHumidity[idx]
                        ZoneCardStyled(
                            zone = item.zone,
                            humidity = item.humidity
                        ) {
                            onSelect(item.zone.id)
                        }
                    }
                }

            }
        }
    }
}

/* ---------- Tarjeta de alerta ---------- */
@Composable
fun AlertCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "⚠️  $message",
            color = Color.White,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/* ---------- Tarjetas de estado ---------- */
@Composable
fun StatusCard(title: String, value: String, icon: Int) {
    val white = colorResource(id = R.color.white)
    val greenPrimary = colorResource(id = R.color.green_primary)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = white)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = greenPrimary
            )
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(value, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}


/* ---------- Tarjeta de zona con diseño ---------- */
@Composable
fun ZoneCardStyled(zone: Zone, humidity: Int?, onClick: () -> Unit) {
    val white = colorResource(id = R.color.white)
    val greenPrimary = colorResource(id = R.color.green_primary)
    val humidityPct = (humidity ?: 0).coerceIn(0, 100) / 100f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = white),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plant),
                contentDescription = null,
                tint = greenPrimary,
                modifier = Modifier.size(28.dp)
            )
            CircularPercentage(progress = humidityPct, size = 56.dp)
            Text(
                "Zona ${zone.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Humedad ${humidity ?: "--"}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


/* ---------- Indicador circular de porcentaje ---------- */
@Composable
fun CircularPercentage(progress: Float, size: Dp) {
    val greenPrimary = colorResource(id = R.color.green_primary)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.LightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = greenPrimary,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(text = "${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
    }
}
