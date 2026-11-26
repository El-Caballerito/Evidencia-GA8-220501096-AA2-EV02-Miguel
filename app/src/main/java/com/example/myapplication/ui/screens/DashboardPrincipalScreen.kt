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
import com.example.myapplication.data.local.entity.*
import com.example.myapplication.domain.usecase.IaAuditorUseCase
import com.example.myapplication.ui.theme.*

@Composable
fun DashboardPrincipalScreen(
    empleado: EmpleadoEntity,
    marcaciones: List<MarcacionEntity>,
    turnos: List<HorarioTurnoEntity>,
    justificacionesEmp: List<JustificacionEntity>,
    pendientesAdmin: List<JustificacionEntity>,
    empleadosSede: List<EmpleadoEntity>,
    iaUseCase: IaAuditorUseCase,
    onMarcar: (String) -> Unit,
    onCrearJustificacion: (String, String, String?) -> Unit,
    onDecidirJustificacion: (JustificacionEntity, Boolean) -> Unit,
    onAuditarNomina: ((String) -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val resumen = remember(marcaciones) { iaUseCase.calcularResumenJornada(marcaciones) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        GrowthTopBar(onLogout = onLogout)
        TabRow(selectedTabIndex = tab, containerColor = GrowthPurpleLight, contentColor = GrowthPurplePrimary) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Inicio") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Justificar") })
            if (empleado.esAdmin) {
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Supervisor") })
            }
        }

        when (tab) {
            0 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Hola, ${empleado.nombre}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Rol: ${empleado.rol} | ID: ${empleado.codigoId}", fontSize = 13.sp, color = Color.Gray)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { onMarcar("Entrada") }, colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black), modifier = Modifier.weight(1f)) { Text("Entrada", fontSize = 11.sp) }
                        Button(onClick = { onMarcar("Inicio Almuerzo") }, colors = ButtonDefaults.buttonColors(containerColor = GrowthYellowLunch, contentColor = Color.Black), modifier = Modifier.weight(1f)) { Text("Almuerzo", fontSize = 11.sp) }
                        Button(onClick = { onMarcar("Salida") }, colors = ButtonDefaults.buttonColors(containerColor = GrowthRedSoft, contentColor = GrowthAlertErrorText), modifier = Modifier.weight(1f)) { Text("Salida", fontSize = 11.sp) }
                    }
                }
                item {
                    Surface(color = GrowthInputGray, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(resumen.normales, fontWeight = FontWeight.Bold); Text("Normales", fontSize = 12.sp) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(resumen.extras, fontWeight = FontWeight.Bold); Text("Extras", fontSize = 12.sp) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(resumen.faltantes, fontWeight = FontWeight.Bold); Text("Faltantes", fontSize = 12.sp) }
                        }
                    }
                }
                items(turnos) { t ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t.diaSemana, fontWeight = FontWeight.Medium)
                        Text(if (t.esDescanso) "Descanso" else "${t.horaInicio} - ${t.horaFin}", color = if (t.esDescanso) GrowthPurplePrimary else Color.DarkGray)
                    }
                }
            }
            1 -> {
                var tipo by remember { mutableStateOf("Cita medica") }
                var desc by remember { mutableStateOf("") }
                var adjunto by remember { mutableStateOf("soporte_eps.pdf") }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text("Nueva Justificación (IA Local)", fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
                        OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = adjunto, onValueChange = { adjunto = it }, label = { Text("Soporte") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = { if (desc.isNotBlank()) { onCrearJustificacion(tipo, desc, adjunto.ifBlank { null }); desc = "" } },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Enviar Justificación", fontWeight = FontWeight.Bold) }
                    }
                    items(justificacionesEmp) { j ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${j.tipoNovedad} — ${j.estado}", fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
                                Text(j.observacionRevision ?: j.descripcion, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            2 -> {
                var justVisor by remember { mutableStateOf<JustificacionEntity?>(null) }
                var msgNomina by remember { mutableStateOf<String?>(null) }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text("Nómina (${empleado.sede})", fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
                        Text("Activos: ${empleadosSede.size} | Pendientes: ${pendientesAdmin.size}", fontSize = 13.sp)
                        Button(onClick = { onAuditarNomina { msgNomina = it } }, colors = ButtonDefaults.buttonColors(containerColor = GrowthPurplePrimary), modifier = Modifier.fillMaxWidth()) {
                            Text("Ejecutar Auditoría IA de Nómina")
                        }
                        if (msgNomina != null) Text(msgNomina!!, color = GrowthAlertSuccessText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    items(pendientesAdmin) { p ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) { Text(p.tipoNovedad, fontWeight = FontWeight.Bold); Text(p.descripcion, fontSize = 12.sp) }
                                Button(onClick = { justVisor = p }) { Text("Revisar") }
                            }
                        }
                    }
                }
                if (justVisor != null) {
                    VisorSoporteObligatorioDialog(
                        justificacion = justVisor!!,
                        onCerrar = { justVisor = null },
                        onAprobar = { onDecidirJustificacion(justVisor!!, true); justVisor = null },
                        onRechazar = { onDecidirJustificacion(justVisor!!, false); justVisor = null }
                    )
                }
            }
        }
    }
}
