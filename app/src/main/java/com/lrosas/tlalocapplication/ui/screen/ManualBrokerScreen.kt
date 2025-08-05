package com.lrosas.tlalocapplication.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lrosas.tlalocapplication.R
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.store.BrokerCreds
import com.lrosas.tlalocapplication.data.store.UserPrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualBrokerScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var host by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("tlaloc") }

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

    Scaffold(
        containerColor = greenPrimary,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Credenciales MQTT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = white
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padd ->
        Card(
            modifier = Modifier
                .padding(padd)
                .padding(24.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = white),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it.trim() },
                    label = { Text("Host (sin tls://)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it.trim() },
                    label = { Text("Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it.trim() },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it.trim() },
                    label = { Text("Topic base (ej. tlaloc)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancelar", color = Color.Gray)
                    }

                    Button(
                        enabled = host.isNotBlank() && user.isNotBlank() && pass.isNotBlank() && topic.isNotBlank(),
                        onClick = {
                            scope.launch {
                                UserPrefs.saveBroker(ctx, host, user, pass, topic)
                                HiveMqManager.overrideCreds(BrokerCreds(host, user, pass, topic))
                                HiveMqManager.connect()
                                Toast.makeText(ctx, "Broker guardado", Toast.LENGTH_SHORT).show()
                                onSaved()
                            }
                        },
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
