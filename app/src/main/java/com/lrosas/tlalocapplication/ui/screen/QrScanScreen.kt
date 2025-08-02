package com.lrosas.tlalocapplication.ui.screen

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Abre la cámara al entrar en pantalla
    SideEffect {
        launcher.launch(ScanOptions())
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        TextButton(onClick = onManual) {
            Text("Introducir manualmente")
        }
    }
}
