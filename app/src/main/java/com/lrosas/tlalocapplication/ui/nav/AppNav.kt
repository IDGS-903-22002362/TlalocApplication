package com.lrosas.tlalocapplication.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lrosas.tlalocapplication.ui.SensorTestScreen
import com.lrosas.tlalocapplication.ui.screen.CalibrateHumScreen
import com.lrosas.tlalocapplication.ui.screen.HistoryScreen
import com.lrosas.tlalocapplication.ui.screen.ManualBrokerScreen   // ← **NUEVA pantalla**
import com.lrosas.tlalocapplication.ui.screen.QrScanScreen
import com.lrosas.tlalocapplication.ui.screen.SignInScreen

/* ─────────────── Rutas ─────────────── */
sealed class Route(val r: String) {
    object Login     : Route("login")
    object Scan      : Route("scan")
    object Manual    : Route("manual")    // ← credenciales manuales
    object Test      : Route("test")
    object Calibrate : Route("calibrate")
    object History   : Route("history")
}

/* ─────────────── NavHost ─────────────── */
@Composable
fun AppNav(
    startRoute: String,                          // se decide fuera (MainActivity)
    nav: NavHostController = rememberNavController()
) {
    NavHost(nav, startDestination = startRoute) {

        /* ---------- Login (Firebase) ---------- */
        composable(Route.Login.r) {
            SignInScreen {                       // onDone →
                nav.navigate(Route.Scan.r) {
                    popUpTo(Route.Login.r) { inclusive = true }
                }
            }
        }

        /* ---------- Escaneo de QR ---------- */
        composable(Route.Scan.r) {
            QrScanScreen(
                ctx = LocalContext.current,
                onParsed = {                      // QR correcto →
                    nav.navigate(Route.Test.r) {
                        popUpTo(Route.Scan.r) { inclusive = true }
                    }
                },
                onManual = {                      // Usuario prefiere teclear
                    nav.navigate(Route.Manual.r)
                }
            )
        }

        /* ---------- Credenciales manuales ---------- */
        composable(Route.Manual.r) {
            ManualBrokerScreen(
                onSaved = {
                    // → aquí navegamos a Test, borrando Scan y Manual
                    nav.navigate(Route.Test.r) {
                        popUpTo(Route.Scan.r) { inclusive = true }
                    }
                },
                onCancel = {
                    nav.popBackStack()
                }
            )
        }

        /* ---------- Pruebas & lecturas ---------- */
        composable(Route.Test.r) {
            SensorTestScreen { nav.navigate(it) } // it = Route.Calibrate.r / .History.r
        }

        /* ---------- Calibración humedad ---------- */
        composable(Route.Calibrate.r) {
            CalibrateHumScreen { nav.popBackStack() }
        }

        /* ---------- Historial ---------- */
        composable(Route.History.r) {
            HistoryScreen(
                onBack = {
                    nav.popBackStack()
                    Unit
                }
            )
        }
    }
}
