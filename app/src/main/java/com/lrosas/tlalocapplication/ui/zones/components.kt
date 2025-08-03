/*  ui/zones/components.kt  */

package com.lrosas.tlalocapplication.ui.zones

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lrosas.tlalocapplication.data.model.Telemetry
import com.lrosas.tlalocapplication.data.model.Zone
import com.lrosas.tlalocapplication.ui.theme.okGreen
import com.lrosas.tlalocapplication.ui.theme.warningRed
import kotlin.math.roundToInt

/**
 * Tarjeta usada en la pantalla *Inicio* (`ZonesScreen`).
 *
 * @param zone        datos básicos (nombre, emoji…)
 * @param reading     lectura más reciente; `null` si aún no hay datos
 * @param idealHum    humedad ideal para la especie (> 0) para pintar el anillo
 * @param onClick     callback cuando la tarjeta se pulsa
 */
@Composable
fun ZoneCard(
    zone: Zone,
    reading: Telemetry? = null,
    idealHum: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(180.dp)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            /* ---------- nombre de la zona ---------- */
            Text(
                zone.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            /* ---------- anillo de humedad ---------- */
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {

                // borde “gris” base ⭘
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 10.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }

                // si hay lectura, dibuja progreso
                reading?.humidity?.let { h ->
                    val pct = h.coerceIn(0f, 100f)
                    val color =
                        if (idealHum != null && pct in (idealHum - 5f)..(idealHum + 5f))
                            okGreen
                        else warningRed

                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = 360f * (pct / 100f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 10.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    Text(
                        text = "${pct.roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } ?: Text(                      // sin datos todavía
                    "—",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            /* ---------- pie de tarjeta ---------- */
            Text(
                if (reading?.humidity != null) "Humedad ${reading.humidity.roundToInt()} %" else "Sin datos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
