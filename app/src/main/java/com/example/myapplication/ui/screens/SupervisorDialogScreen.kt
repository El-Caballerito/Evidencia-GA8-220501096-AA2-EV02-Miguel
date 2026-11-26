package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.entity.JustificacionEntity
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VisorSoporteObligatorioDialog(
    justificacion: JustificacionEntity,
    onCerrar: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    var segundos by remember { mutableIntStateOf(0) }
    val habilitado = segundos >= 60

    LaunchedEffect(justificacion.id) {
        while (segundos < 60) {
            delay(1000L)
            segundos++
        }
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                text = "Visor de Auditoría (00:${String.format("%02d", minOf(segundos, 60))} / 01:00)",
                color = GrowthPurplePrimary,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Novedad: ${justificacion.tipoNovedad}", fontWeight = FontWeight.Bold)
                Text("Detalle: ${justificacion.descripcion}")
                Text("Soporte adjunto: ${justificacion.archivoSoporte ?: "Ninguno"}")
                Text(
                    text = "Debe leer el soporte al menos 60 segundos para habilitar los botones.",
                    color = GrowthAlertErrorText,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAprobar,
                    enabled = habilitado,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black)
                ) { Text("Aprobar") }
                Button(
                    onClick = onRechazar,
                    enabled = habilitado,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthRedDanger)
                ) { Text("Rechazar") }
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
