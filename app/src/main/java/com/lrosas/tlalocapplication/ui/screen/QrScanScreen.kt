package com.lrosas.tlalocapplication.ui.screen

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lrosas.tlalocapplication.R
import com.lrosas.tlalocapplication.data.store.UserPrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    ctx: Context,
    onParsed: () -> Unit,
    onManual: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { raw ->
            val parts = raw.split(";")
            if (parts.size == 4) {
                scope.launch {
                    UserPrefs.saveBroker(ctx, parts[0], parts[1], parts[2], parts[3])
                    onParsed()
                }
            }
        }
    }

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

    Scaffold(
        containerColor = greenPrimary,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Conexión al servidor",
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
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = white)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Escanea el código QR proporcionado para conectar con el servidor MQTT.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = { launcher.launch(ScanOptions()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenPrimary,
                            contentColor = white
                        )
                    ) {
                        Text("Escanear QR")
                    }

                    OutlinedButton(
                        onClick = onManual,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = greenPrimary
                        )
                    ) {
                        Text("Introducir manualmente")
                    }
                }
            }
        }
    }
}
