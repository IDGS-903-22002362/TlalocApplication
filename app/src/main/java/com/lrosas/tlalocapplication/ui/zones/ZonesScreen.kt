package com.lrosas.tlalocapplication.ui.zones

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lrosas.tlalocapplication.ui.components.ZoneCard   // ← ¡ahora sí existe!

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ZonesScreen(
    onAdd: () -> Unit,
    onSelect: (String) -> Unit,
    vm: ZonesViewModel = viewModel()
) {
    val zones by vm.zones.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Inicio") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { pad ->

        if (zones.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) {
                ExtendedFloatingActionButton(onClick = onAdd) { Text("Añadir zona") }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.padding(pad)
            ) {
                items(zones.size) { idx ->
                    ZoneCard(zone = zones[idx]) { onSelect(zones[idx].id) }
                }
            }
        }
    }
}
