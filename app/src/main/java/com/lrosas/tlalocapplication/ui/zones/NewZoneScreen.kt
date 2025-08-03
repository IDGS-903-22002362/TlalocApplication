package com.lrosas.tlalocapplication.ui.zones

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lrosas.tlalocapplication.data.model.Plant
import com.lrosas.tlalocapplication.data.model.Zone
import com.lrosas.tlalocapplication.data.repository.ZoneRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)         // 👈  opt-in a la API experimental
@Composable
fun NewZoneScreen(
    onSaved: () -> Unit,
    onPickPlant: () -> Unit = {},
    vm: NewZoneViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva zona") }) }
    ) { pad ->

        Column(
            Modifier
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* --- nombre de la zona --- */
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la zona") }
            )

            /* --- elegir planta --- */
            Button(onClick = onPickPlant) {
                Text(vm.selectedPlant?.name ?: "Elegir planta")
            }

            /* --- guardar zona --- */
            Button(
                enabled = name.isNotBlank() && vm.selectedPlant != null,
                onClick = {
                    scope.launch {
                        vm.saveZone(name.trim())
                        onSaved()
                    }
                }
            ) { Text("Guardar") }
        }
    }
}

/* ---------- ViewModel ---------- */
class NewZoneViewModel(
    private val zoneRepo: ZoneRepository = ZoneRepository()
) : ViewModel() {

    var selectedPlant by mutableStateOf<Plant?>(null)
        private set

    fun selectPlant(p: Plant) { selectedPlant = p }

    suspend fun saveZone(name: String) {
        val plantId = selectedPlant!!.id
        zoneRepo.addZone(Zone(name = name, plantId = plantId))
    }
}
