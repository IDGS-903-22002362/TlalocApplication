package com.lrosas.tlalocapplication.ui.nav

/* ---------- Compose / Navigation ---------- */
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/* ---------- Pantallas flujo inicial ---------- */
import com.lrosas.tlalocapplication.ui.SensorTestScreen
import com.lrosas.tlalocapplication.ui.screen.CalibrateHumScreen
import com.lrosas.tlalocapplication.ui.screen.HistoryScreen
import com.lrosas.tlalocapplication.ui.screen.ManualBrokerScreen
import com.lrosas.tlalocapplication.ui.screen.QrScanScreen
import com.lrosas.tlalocapplication.ui.screen.SignInScreen

/* ---------- Nuevas pantallas ---------- */
import com.lrosas.tlalocapplication.ui.zones.ZonesScreen
import com.lrosas.tlalocapplication.ui.zones.NewZoneScreen
import com.lrosas.tlalocapplication.ui.zones.ZoneDetailScreen
import com.lrosas.tlalocapplication.ui.plants.PlantsLibraryScreen
import com.lrosas.tlalocapplication.ui.plants.NewPlantScreen

/* ─────────────── Rutas ─────────────── */
sealed class Route(val r: String) {
    object Login     : Route("login")
    object Scan      : Route("scan")
    object Manual    : Route("manual")
    object Test      : Route("test")
    object Calibrate : Route("calibrate")
    object History   : Route("history")

    object Zones      : Route("zones")
    object AddZone    : Route("zones/add")
    object Plants     : Route("plants")
    object AddPlant   : Route("plants/add")

    object ZoneDetail : Route("zones/{zoneId}") {
        fun create(zoneId: String) = "zones/$zoneId"
    }
}

/* ─────────────── NavHost ─────────────── */
@Composable
fun AppNav(
    startRoute: String,
    nav: NavHostController = rememberNavController()
) {
    NavHost(nav, startDestination = startRoute) {

        /* ---------- Login ---------- */
        composable(Route.Login.r) {
            SignInScreen {
                nav.navigate(Route.Scan.r) {
                    popUpTo(Route.Login.r) { inclusive = true }
                }
            }
        }

        /* ---------- Escaneo / Manual ---------- */
        composable(Route.Scan.r) {
            QrScanScreen(
                ctx = LocalContext.current,
                onParsed = {
                    nav.navigate(Route.Test.r) {
                        popUpTo(Route.Scan.r) { inclusive = true }
                    }
                },
                onManual = { nav.navigate(Route.Manual.r) }
            )
        }
        composable(Route.Manual.r) {
            ManualBrokerScreen(
                onSaved = {
                    nav.navigate(Route.Test.r) {
                        popUpTo(Route.Scan.r) { inclusive = true }
                    }
                },
                onCancel = { nav.popBackStack() }
            )
        }

        /* ---------- Test & derivadas ---------- */
        composable(Route.Test.r) {
            SensorTestScreen(
                onNavigate = { nav.navigate(it) },          // Calibrate / History
                onConfirm  = {                              // nuevo botón “Continuar”
                    nav.navigate(Route.Zones.r) {
                        popUpTo(Route.Test.r) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Calibrate.r) { CalibrateHumScreen { nav.popBackStack() } }
        composable(Route.History.r)   { HistoryScreen(onBack = { nav.popBackStack() }) }

        /* ========== ZONAS / PLANTAS ========== */
        composable(Route.Zones.r) {
            ZonesScreen(
                onAdd    = { nav.navigate(Route.AddZone.r) },
                onSelect = { id -> nav.navigate(Route.ZoneDetail.create(id)) }
            )
        }

        composable(Route.AddZone.r) {
            NewZoneScreen(
                onSaved     = { nav.popBackStack() },
                onPickPlant = { nav.navigate(Route.Plants.r) }
            )
        }

        composable(Route.Plants.r) {
            PlantsLibraryScreen(
                onSelect = { plant ->
                    nav.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedPlant", plant)
                    nav.popBackStack()
                },
                onAdd = { nav.navigate(Route.AddPlant.r) }
            )
        }

        composable(Route.AddPlant.r) {
            NewPlantScreen(onSaved = { nav.popBackStack() })
        }

        composable(Route.ZoneDetail.r) { back ->
            val zoneId = back.arguments?.getString("zoneId") ?: return@composable
            ZoneDetailScreen(zoneId = zoneId, onBack = { nav.popBackStack() })
        }
    }
}
