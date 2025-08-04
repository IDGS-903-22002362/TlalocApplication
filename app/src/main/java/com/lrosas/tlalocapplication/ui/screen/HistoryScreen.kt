package com.lrosas.tlalocapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.lrosas.tlalocapplication.ui.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    zoneId: String,
    onBack: () -> Unit
) {
    // 1) Creamos el VM pasando zoneId al constructor
    val vm: HistoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(zoneId) as T
            }
        }
    )

    // 2) Observamos la lista de lecturas
    val data by vm.readings.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial (24 h)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (data.isEmpty()) {
            // Cargando o sin datos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Gráfica de línea
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(12.dp),
                    factory = { ctx ->
                        LineChart(ctx).apply {
                            description = Description().apply { text = "" }
                            axisRight.isEnabled = false
                            legend.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val entries = data.mapIndexed { i, reading ->
                            Entry(i.toFloat(), reading.humPct.toFloat())
                        }
                        val set = LineDataSet(entries, "Humedad %").apply {
                            setDrawValues(false)
                            setDrawCircles(false)
                            colors = ColorTemplate.MATERIAL_COLORS.toList()
                        }
                        chart.data = LineData(set)
                        chart.invalidate()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para refrescar manualmente
                Button(
                    onClick = { vm.refresh() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Actualizar")
                }
            }
        }
    }
}
