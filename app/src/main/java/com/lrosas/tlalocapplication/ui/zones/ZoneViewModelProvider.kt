/* ui/zones/ZoneViewModelProvider.kt */

package com.lrosas.tlalocapplication.ui.zones

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

/**
 * Devuelve exactamente el mismo `ZonesViewModel` creado en `ZonesScreen`
 * (raíz de la sección de zonas) usando el `backStackEntry` de dicha ruta.
 */
@Composable
fun rememberSharedZonesViewModel(
    backEntry: NavBackStackEntry
): ZonesViewModel {
    // El key = ruta asegura que sea compartido dentro de esa entrada
    return viewModel(
        viewModelStoreOwner = backEntry
    )
}
