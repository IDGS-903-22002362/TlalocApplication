package com.lrosas.tlalocapplication.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.store.UserPrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualBrokerScreen(
    onSaved: () -> Unit,          // navega a Test
    onCancel: () -> Unit          // vuelve atrás
) {
    val ctx   = LocalContext.current
    val scope = rememberCoroutineScope()

    /* ---------- estado de los cuatro campos ---------- */
    var host  by remember { mutableStateOf("") }
    var user  by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("tlaloc") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credenciales MQTT") }
            )
        }
    ) { padd ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = host, onValueChange = { host = it.trim() },
                label = { Text("Host (sin tls://)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = user, onValueChange = { user = it.trim() },
                label = { Text("Usuario") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pass, onValueChange = { pass = it.trim() },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = topic, onValueChange = { topic = it.trim() },
                label = { Text("Topic base (ej. tlaloc)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            /* ---------- botones ---------- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Button(
                    enabled = host.isNotBlank() && user.isNotBlank()
                            && pass.isNotBlank() && topic.isNotBlank(),
                    onClick = {
                        scope.launch {
                            /* guarda en DataStore */
                            UserPrefs.saveBroker(ctx, host, user, pass, topic)
                            /* actualiza HiveMqManager inmediatamente */
                            HiveMqManager.overrideCreds(
                                com.lrosas.tlalocapplication.data.store.BrokerCreds(
                                    host, user, pass, topic
                                )
                            )
                            HiveMqManager.connect()

                            Toast.makeText(ctx, "Broker guardado", Toast.LENGTH_SHORT).show()
                            onSaved()
                        }
                    }
                ) { Text("Guardar") }
            }
        }
    }
}
