package com.lrosas.tlalocapplication.ui.plants

/* ---------- Compose ---------- */
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

/* ---------- Dominio ---------- */
import com.lrosas.tlalocapplication.data.model.Care
import com.lrosas.tlalocapplication.data.repository.PlantRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlantScreen(
    onSaved: () -> Unit,
    vm: NewPlantViewModel = viewModel()
) {
    /* ---------- estado local ---------- */
    var name     by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var lux      by remember { mutableStateOf("") }
    var tds      by remember { mutableStateOf("") }
    var level    by remember { mutableStateOf("") }
    val scope    = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva planta") }) }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") }
            )
            OutlinedTextField(
                value = humidity,
                onValueChange = { humidity = it },
                label = { Text("Humedad ideal %") }
            )
            OutlinedTextField(
                value = lux,
                onValueChange = { lux = it },
                label = { Text("Lux ideal") }
            )
            OutlinedTextField(
                value = tds,
                onValueChange = { tds = it },
                label = { Text("Calidad agua ideal ppm") }
            )
            OutlinedTextField(
                value = level,
                onValueChange = { level = it },
                label = { Text("Nivel depósito ideal cm") }
            )

            Button(
                enabled = name.isNotBlank(),
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
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
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
