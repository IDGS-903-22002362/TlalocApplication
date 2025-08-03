/*  ui/zones/NewZoneScreen.kt  */
package com.lrosas.tlalocapplication.ui.zones

/* ---------- Compose ---------- */
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/* ---------- Navigation ---------- */
import androidx.navigation.NavHostController

/* ---------- MVVM ---------- */
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/* ---------- Data ---------- */
import com.lrosas.tlalocapplication.data.model.Plant
import com.lrosas.tlalocapplication.data.model.Zone
import com.lrosas.tlalocapplication.data.repository.PlantRepository
import com.lrosas.tlalocapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewZoneScreen(
    navController : NavHostController,
    onSaved       : () -> Unit,
    onPickPlant   : () -> Unit = {},
    vm            : NewZoneViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    val scope  = rememberCoroutineScope()

    /* 1 ► recibir **el id** de la planta elegida */
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<String?>("selectedPlantId", null)   // ← String
            ?.collectLatest { id ->
                if (id != null) {
                    vm.loadPlant(id)                           // carga desde repo
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedPlantId", null)         // limpiar
                }
            }
    }


    /* ── 2 ▸ UI ───────────────────────────────────────────────────────────────── */
    Scaffold(topBar = { TopAppBar(title = { Text("Nueva zona") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Nombre de la zona") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onPickPlant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(vm.selectedPlant?.name ?: "Elegir planta")
            }

            Button(
                enabled = name.isNotBlank() && vm.selectedPlant != null,
                onClick = {
                    scope.launch {
                        vm.saveZone(name.trim())
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}

/* ───────────────────────────── ViewModel ───────────────────────────── */
class NewZoneViewModel(
    private val zoneRepo  : ZoneRepository  = ZoneRepository(),
    private val plantRepo : PlantRepository = PlantRepository()
) : ViewModel() {

    var selectedPlant by mutableStateOf<Plant?>(null)
        private set

    suspend fun loadPlant(id: String) {
        selectedPlant = plantRepo.getPlant(id)
    }


    suspend fun saveZone(name: String) {
        val plantId = selectedPlant!!.id                   // ya existe
        zoneRepo.addZone(Zone(name = name, plantId = plantId))
    }
}


