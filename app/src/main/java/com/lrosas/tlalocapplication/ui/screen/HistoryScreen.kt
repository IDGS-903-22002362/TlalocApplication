package com.lrosas.tlalocapplication.ui.screen

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.charts.LineChart
import androidx.compose.ui.viewinterop.AndroidView
import com.lrosas.tlalocapplication.ui.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit,
                  vm: HistoryViewModel = viewModel()) {

    val data by vm.readings.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial (24 h)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        if (data.isEmpty()) Box(Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else Column(Modifier.fillMaxSize().padding(padding)) {

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
                    val entries = data.mapIndexed { i, r ->
                        Entry(i.toFloat(), r.humPct.toFloat())
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
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.refresh() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Actualizar") }
        }
    }
}