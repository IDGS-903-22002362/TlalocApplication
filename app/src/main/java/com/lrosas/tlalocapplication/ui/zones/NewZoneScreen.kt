package com.lrosas.tlalocapplication.ui.zones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.lrosas.tlalocapplication.R
import com.lrosas.tlalocapplication.data.model.Plant
import com.lrosas.tlalocapplication.data.model.Zone
import com.lrosas.tlalocapplication.data.repository.PlantRepository
import com.lrosas.tlalocapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewZoneScreen(
    navController: NavHostController,
    onSaved: () -> Unit,
    onBack: () -> Unit = {},
    onPickPlant: () -> Unit = {},
    vm: NewZoneViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    val suggested by vm.suggestedId.collectAsState()
    var zoneId by remember(suggested) { mutableStateOf(suggested) }
    val scope = rememberCoroutineScope()

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(greenPrimary)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Agregar Zona",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = white
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = white)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Card(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = white)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        "Nueva Zona",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = greenPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la zona") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = zoneId,
                        onValueChange = { zoneId = it.trim() },
                        label = { Text("ID interno (único)") },
                        supportingText = { Text("Ej.: zone1, huertoA…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    // Selector de planta
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Planta",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        OutlinedButton(
                            onClick = onPickPlant,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.dp, Color.Gray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    vm.selectedPlant?.name ?: "Seleccionar planta",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray
                        )

                        Text(
                            "Usar valores predeterminados",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    zoneId = suggested
                                    name = ""
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, greenPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = greenPrimary
                                )
                            ) {
                                Text("Restaurar")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        vm.saveZone(zoneId.trim(), name.trim())
                                        onSaved()
                                    }
                                },
                                enabled = name.isNotBlank() &&
                                        zoneId.isNotBlank() &&
                                        vm.selectedPlant != null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = greenPrimary,
                                    contentColor = white
                                )
                            ) {
                                Text("Guardar")
                            }
                        }
                    }
                }
            }
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
