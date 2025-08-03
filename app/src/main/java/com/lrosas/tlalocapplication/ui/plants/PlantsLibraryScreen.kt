package com.lrosas.tlalocapplication.ui.plants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lrosas.tlalocapplication.data.model.Plant
import com.lrosas.tlalocapplication.data.repository.PlantRepository
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsLibraryScreen(
    onSelect: (Plant) -> Unit,
    onAdd: () -> Unit,
    vm: PlantsViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val plants by vm.plants.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Biblioteca") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd) { Text("+ Nueva planta") }
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {

            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier = Modifier.fillMaxWidth()
            ) {}

            Spacer(Modifier.height(8.dp))

            LazyColumn(contentPadding = PaddingValues(8.dp)) {
                val filtered = plants.filter { it.name.contains(query, true) }
                items(filtered.size) { idx ->
                    PlantRow(filtered[idx]) { onSelect(filtered[idx]) }
                }
            }
        }
    }
}

@Composable
private fun PlantRow(plant: Plant, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        /* 👇 cambio clave: headlineContent en lugar de headlineText */
        ListItem(
            headlineContent = { Text(plant.name) }
        )
    }
}

/* ---------- ViewModel ---------- */
class PlantsViewModel(
    private val repo: PlantRepository = PlantRepository()
) : ViewModel() {

    val plants = repo.getAllPlants()          // Flow<List<Plant>>
}
