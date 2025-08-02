package com.lrosas.tlalocapplication.ui.screen
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lrosas.tlalocapplication.data.store.UserPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun QrScanScreen(
    ctx: Context,
    onParsed: () -> Unit,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { res ->
            res?.contents?.let { raw ->
                val parts = raw.split(";")
                if (parts.size == 4) scope.launch {
                    UserPrefs.saveBroker(ctx, parts[0], parts[1], parts[2], parts[3])
                    onParsed()
                }
            }
        }
    )

    SideEffect { launcher.launch(ScanOptions()) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Abriendo cámara…")
    }
}
