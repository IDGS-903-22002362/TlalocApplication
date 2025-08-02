package com.lrosas.tlalocapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lrosas.tlalocapplication.core.mqtt.HiveMqManager
import com.lrosas.tlalocapplication.data.store.UserPrefs
import com.lrosas.tlalocapplication.ui.SensorTestScreen
import com.lrosas.tlalocapplication.ui.nav.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(b:Bundle?) {
        super.onCreate(b)
        setContent{ MaterialTheme{
            val ctx = LocalContext.current
            // cuando DataStore ya tenga credenciales, conéctate
            LaunchedEffect(Unit) {
                UserPrefs.brokerFlow(ctx).collect { creds ->
                    creds?.let {
                        HiveMqManager.overrideCreds(it)
                        HiveMqManager.connect()
                    }
                }
            }

            AppNav()
        }}
    }
}
