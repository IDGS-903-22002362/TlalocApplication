package com.lrosas.tlalocapplication.ui.plants

/* ---------- Compose ---------- */
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.lrosas.tlalocapplication.R

/* ---------- Dominio ---------- */
import com.lrosas.tlalocapplication.data.model.Care
import com.lrosas.tlalocapplication.data.repository.PlantRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlantScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit = {},
    vm: NewPlantViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var lux by remember { mutableStateOf("") }
    var tds by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

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
                            "Agregar Planta",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = white
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = white)
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Nueva Planta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = greenPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = humidity,
                        onValueChange = { humidity = it },
                        label = { Text("Humedad ideal (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = lux,
                        onValueChange = { lux = it },
                        label = { Text("Lux ideal") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = tds,
                        onValueChange = { tds = it },
                        label = { Text("Calidad agua ideal (ppm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("Nivel del depósito (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                vm.save(
                                    name.trim(),
                                    humidity.toIntOrNull() ?: 50,
                                    lux.toFloatOrNull() ?: 1000f,
                                    tds.toFloatOrNull() ?: 0f,
                                    level.toFloatOrNull() ?: 0f
                                )
                                onSaved()
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
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

/* ---------- ViewModel ---------- */
class NewPlantViewModel(
    private val repo: PlantRepository = PlantRepository()
) : ViewModel() {

    suspend fun save(
        name: String,
        humidity: Int,
        lux: Float,
        tds: Float,
        level: Float
    ) {
        val id = repo.addPlant(name)
        repo.saveCare(
            Care(
                plantId         = id,
                humidity        = humidity,
                light           = lux,
                waterQuality    = tds,
                waterLevelIdeal = level
            )
        )
    }
}