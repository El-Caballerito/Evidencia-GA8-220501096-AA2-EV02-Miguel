package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.entity.MarcacionEntity
import com.example.myapplication.domain.usecase.ResumenJornada
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialAsistenciaScreen(marcaciones: List<MarcacionEntity>, onVolver: () -> Unit) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Historial de Asistencia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        if (marcaciones.isEmpty()) {
            Text("Aún no hay marcaciones registradas.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(marcaciones) { m ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(m.tipoEvento, fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
                                Text("Fecha: ${m.fecha}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(sdf.format(Date(m.horaRegistro)), fontWeight = FontWeight.Bold)
                                Text(if (m.geocercaValida) "GPS OK" else "Fuera GPS", fontSize = 11.sp, color = Color(0xFF15803D))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenHorasScreen(resumen: ResumenJornada, marcaciones: List<MarcacionEntity>, onVolver: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Resumen de Horas (Ley 2101)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Surface(color = GrowthPurpleLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Jornada Base: 7h 00m (42h semanales)", fontWeight = FontWeight.SemiBold, color = GrowthPurplePrimary)
                Text("Horas Normales: ${resumen.normales}", fontWeight = FontWeight.Bold)
                Text("Horas Extras: ${resumen.extras}", color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                Text("Horas Faltantes: ${resumen.faltantes}", color = GrowthAlertErrorText, fontWeight = FontWeight.Bold)
                Text("Minutos Efectivos: ${resumen.minutosTrabajados} min", fontSize = 13.sp)
            }
        }
        Text("Días con registro: ${marcaciones.map { it.fecha }.distinct().size}", fontSize = 13.sp, color = Color.Gray)
    }
}
