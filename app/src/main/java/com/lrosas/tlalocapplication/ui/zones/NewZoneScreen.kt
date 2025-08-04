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

/* ---------- View-model ---------- */
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel

/* ---------- Repositorios / modelos ---------- */
import com.lrosas.tlalocapplication.data.model.Plant
import com.lrosas.tlalocapplication.data.model.Zone
import com.lrosas.tlalocapplication.data.repository.PlantRepository
import com.lrosas.tlalocapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

/* ─────────────────────────────── UI ──────────────────────────────── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewZoneScreen(
    navController: NavHostController,
    onSaved:      () -> Unit,
    onPickPlant:  () -> Unit = {},
    vm:           NewZoneViewModel = viewModel()
) {
    /* ---------- estados locales ---------- */
    var name by remember { mutableStateOf("") }

    /*  Sugerencia de ID: se reinicia cada vez que el VM calcule uno nuevo  */
    val suggested by vm.suggestedId.collectAsState()
    var zoneId by remember(suggested) { mutableStateOf(suggested) }

    val scope = rememberCoroutineScope()

    /* ---------- escuchar el id de planta devuelto desde la biblioteca ---------- */
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<String?>("selectedPlantId", null)
            ?.collectLatest { id ->
                id?.let {
                    vm.loadPlant(it)
                    navController.currentBackStackEntry?.savedStateHandle
                        ?.set("selectedPlantId", null)
                }
            }
    }

    /* ----------------------------- UI ----------------------------- */
    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva zona") }) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /* Nombre visible */
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la zona") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            /* ID interno (editable) */
            OutlinedTextField(
                value = zoneId,
                onValueChange = { zoneId = it.trim() },
                label = { Text("ID interno (único)") },
                supportingText = { Text("Ej.: zone1, huertoA…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            /* Elegir planta */
            Button(
                onClick = onPickPlant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(vm.selectedPlant?.name ?: "Elegir planta")
            }

            /* Guardar */
            Button(
                enabled = name.isNotBlank() &&
                        zoneId.isNotBlank() &&
                        vm.selectedPlant != null,
                onClick = {
                    scope.launch {
                        vm.saveZone(
                            desiredId = zoneId,
                            name      = name.trim()
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}

/* ─────────────────────────–– ViewModel ─────────────────────────–– */
class NewZoneViewModel(
    private val zoneRepo : ZoneRepository  = ZoneRepository(),
    private val plantRepo: PlantRepository = PlantRepository()
) : ViewModel() {

    /* -------- 1) planta elegida -------- */
    var selectedPlant by mutableStateOf<Plant?>(null)
        private set

    suspend fun loadPlant(id: String) {
        selectedPlant = plantRepo.getPlant(id)
    }

    /* -------- 2) sugerencia automática -------- */
    private val _suggestedId = MutableStateFlow("…")
    val suggestedId: StateFlow<String> = _suggestedId

    init {
        viewModelScope.launch {
            val seq = runCatching { zoneRepo.nextSequentialNumber() }
                .getOrDefault(1)
            _suggestedId.value = "zone$seq"
        }
    }

    /* -------- 3) guardar -------- */
    suspend fun saveZone(desiredId: String, name: String) {
        val plantId = selectedPlant!!.id          // botón habilitado ⇢ no es null
        zoneRepo.addZoneWithId(
            id   = desiredId,
            zone = Zone(name = name, plantId = plantId)
        )
    }
}
