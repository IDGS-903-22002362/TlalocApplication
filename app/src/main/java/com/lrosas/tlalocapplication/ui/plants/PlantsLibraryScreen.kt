package com.lrosas.tlalocapplication.ui.plants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lrosas.tlalocapplication.data.model.Plant
import com.lrosas.tlalocapplication.data.repository.PlantRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsLibraryScreen(
    onSelect: (String) -> Unit,    // ← se mantiene la devolución por ID
    onAdd: () -> Unit,
    vm: PlantsViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val plants by vm.plants.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color(0xFF174B26), // Fondo verde oscuro
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Biblioteca",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF174B26),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir planta",
                    tint = Color(0xFF174B26)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Buscador
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar plantas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Lista
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filtered = plants.filter { it.name.contains(query, ignoreCase = true) }

                items(filtered, key = { it.id }) { plant ->
                    PlantCard(
                        plant = plant,
                        onClick = { onSelect(plant.id) } // ← enviamos solo el ID
                    )
                }
            }
        }
    }
}

/* Tarjeta individual */
@Composable
private fun PlantCard(plant: Plant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        )
    }
}

/* ViewModel */
class PlantsViewModel(
    private val repo: PlantRepository = PlantRepository()
) : ViewModel() {
    val plants = repo.getAllPlants() // Flow<List<Plant>>
}
