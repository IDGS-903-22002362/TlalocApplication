package com.lrosas.tlalocapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lrosas.tlalocapplication.data.model.Zone

/**
 * Pequeña tarjeta cuadrada para mostrar una zona.
 * Se hace “clickable” para navegar al detalle.
 */
@Composable
fun ZoneCard(
    zone: Zone,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)           // cuadrada
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation()
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(zone.name)
        }
    }
}
