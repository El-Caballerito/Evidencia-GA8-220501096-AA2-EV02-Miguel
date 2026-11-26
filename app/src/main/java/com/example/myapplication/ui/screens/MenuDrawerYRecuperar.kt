package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.entity.SolicitudPasswordEntity
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RecuperarPasswordScreen(
    onEnviarSolicitud: (SolicitudPasswordEntity) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var nuevaPwd by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Recuperar Contraseña", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("ID o Cédula") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nuevaPwd, onValueChange = { nuevaPwd = it }, label = { Text("Nueva Contraseña Deseada") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = motivo, onValueChange = { motivo = it }, label = { Text("Motivo del bloqueo u olvido") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (nombre.isNotBlank() && id.isNotBlank()) {
                    onEnviarSolicitud(SolicitudPasswordEntity(nombreIngresado = nombre, identificador = id, nuevaPasswordSolicitada = nuevaPwd, motivo = motivo))
                    enviado = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Enviar Solicitud a RR.HH.", fontWeight = FontWeight.Bold) }

        if (enviado) Text("Solicitud enviada a Recursos Humanos.", color = GrowthAlertSuccessText, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onVolver) { Text("Volver al Inicio de Sesión") }
    }
}

@Composable
fun GrowthMenuWrapper(
    esAdmin: Boolean,
    vistaActual: String,
    onSeleccionarVista: (String) -> Unit,
    onLogout: () -> Unit,
    contenido: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val opciones = buildList {
        add("inicio" to "Panel Principal")
        add("historial" to "Historial de Asistencia")
        add("resumen" to "Resumen de Horas (42h)")
        add("configuracion" to "Perfil y Configuración")
        if (esAdmin) add("registro" to "Registrar Empleado")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = GrowthPurpleDrawer, modifier = Modifier.width(260.dp)) {
                Text("MENÚ GROWTH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
                opciones.forEach { (clave, titulo) ->
                    Button(
                        onClick = {
                            onSeleccionarVista(clave)
                            scope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (vistaActual == clave) GrowthGreenAction else GrowthInputGray,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                    ) { Text(titulo, fontWeight = FontWeight.SemiBold) }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthRedDanger, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) { Text("Cerrar Sesión") }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SmallFloatingActionButton(
                onClick = { scope.launch { drawerState.open() } },
                containerColor = GrowthPurplePrimary,
                contentColor = Color.White,
                modifier = Modifier.padding(8.dp)
            ) { Icon(Icons.Default.Menu, contentDescription = "Abrir menú") }
            Box(modifier = Modifier.weight(1f)) { contenido() }
        }
    }
}
