package com.lrosas.tlalocapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.store.UserPrefs
import com.lrosas.tlalocapplication.ui.nav.AppNav
import com.lrosas.tlalocapplication.ui.nav.Route
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ctx = LocalContext.current

            /* ------------ ¿hay broker ya guardado? ------------ */
            //  Usamos 'first()' dentro de un LaunchedEffect para decidir la pantalla inicial
            var startRoute = Route.Login.r
            LaunchedEffect(Unit) {
                if (UserPrefs.hasBroker(ctx)) startRoute = Route.Test.r
            }

            /* ------------ Tema y navegación ------------ */
            MaterialTheme {
                /*  Escucha cualquier cambio de credenciales:
                    - tras escanear QR
                    - tras entrada manual
                 */
                LaunchedEffect(Unit) {
                    UserPrefs.brokerFlow(ctx).collect { creds ->
                        creds?.let {
                            HiveMqManager.overrideCreds(it)
                            HiveMqManager.connect()      // conecta solo una vez
                        }
                    }
                }

                /*  NavHost con la ruta inicial ya decidida  */
                AppNav(startRoute = startRoute)
            }
        }
    }
}
