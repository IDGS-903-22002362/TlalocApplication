package com.lrosas.tlalocapplication.ui.screen

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lrosas.tlalocapplication.data.store.UserPrefs
import kotlinx.coroutines.launch

@Composable
fun QrScanScreen(
    ctx: Context,
    onParsed: () -> Unit,      // dispara cuando se guarden y parseen las credenciales
    onManual: () -> Unit       // dispara al pulsar “Introducir manualmente”
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { launcher.launch(ScanOptions()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Escanear QR")
        }

        OutlinedButton(
            onClick = onManual,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Introducir manualmente")
        }
    }
}
