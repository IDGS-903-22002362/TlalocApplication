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
import com.lrosas.tlalocapplication.ui.screen.QrScanScreen
import com.lrosas.tlalocapplication.ui.screen.SignInScreen

sealed class Route(val r: String) {
    object Login    : Route("login")
    object Scan     : Route("scan")
    object Test     : Route("test")
    object Calibrate: Route("calibrate")
    object History : Route("history")

}

@Composable
fun AppNav(start: String = Route.Login.r) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = start) {

        composable(Route.Login.r) {
            SignInScreen {                      // onDone ->
                nav.navigate(Route.Scan.r) {
                    popUpTo(Route.Login.r) { inclusive = true }
                }
            }
        }

        composable(Route.Scan.r) {
            QrScanScreen(
                ctx = LocalContext.current,
                onParsed = {            // ← cuando el QR es válido
                    nav.navigate(Route.Test.r) {
                        popUpTo(Route.Scan.r) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Test.r) {
            SensorTestScreen { route -> nav.navigate(route) }
        }

        composable(Route.Calibrate.r) {
            CalibrateHumScreen { nav.popBackStack() }
        }
        composable(Route.History.r) {
            HistoryScreen(onBack = { nav.popBackStack() })
        }

    }

}

