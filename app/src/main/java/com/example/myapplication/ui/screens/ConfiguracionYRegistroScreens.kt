package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.entity.*
import com.example.myapplication.domain.usecase.AuthValidatorUseCase
import com.example.myapplication.ui.theme.*

@Composable
fun ConfiguracionPerfilScreen(
    empleado: EmpleadoEntity,
    onActualizarBiometria: (String) -> Unit,
    onVolver: () -> Unit
) {
    var metodoBio by remember { mutableStateOf(empleado.tipoBiometria) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Perfil y Configuración", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nombre: ${empleado.nombre}", fontWeight = FontWeight.Bold)
                Text("ID: ${empleado.codigoId} | Cédula: ${empleado.cedula}")
                Text("Rol: ${empleado.rol} | Área: ${empleado.area}")
                Text("Sede: ${empleado.sede}", fontSize = 12.sp, color = Color.Gray)
            }
        }
        Text("Método Biométrico Preferido", fontWeight = FontWeight.SemiBold, color = GrowthPurplePrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = metodoBio == "Huella", onClick = { metodoBio = "Huella" }, label = { Text("Huella Digital") })
            FilterChip(selected = metodoBio == "Camara", onClick = { metodoBio = "Camara" }, label = { Text("Rostro / Cámara") })
        }
        Button(
            onClick = { onActualizarBiometria(metodoBio); mensaje = "Preferencia biométrica actualizada a $metodoBio" },
            colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar Configuración", fontWeight = FontWeight.Bold) }
        if (mensaje != null) Text(mensaje!!, color = GrowthAlertSuccessText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RegistroEmpleadoScreen(
    authUseCase: AuthValidatorUseCase,
    onRegistrar: (EmpleadoEntity) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("Empleado") }
    var area by remember { mutableStateOf("Operaciones") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Registrar Empleado", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cedula, onValueChange = { cedula = it }, label = { Text("Cédula") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = rol == "Empleado", onClick = { rol = "Empleado" }, label = { Text("Empleado") })
            FilterChip(selected = rol == "Supervisor", onClick = { rol = "Supervisor" }, label = { Text("Supervisor") })
        }
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val cod = authUseCase.generarCodigoId(cedula)
                val (ok, msg) = authUseCase.validarReglaPassword(password, nombre, cod, rol)
                if (!ok) mensaje = "Error: $msg" else {
                    onRegistrar(
                        EmpleadoEntity(
                            codigoId = cod, cedula = cedula.trim(), nombre = nombre.trim(),
                            rol = rol, area = area.trim(), passwordHash = authUseCase.encriptarPassword(password),
                            datoBiometrico = "BIO_REGISTRADA_OK"
                        )
                    )
                    mensaje = "Creado con ID $cod"; nombre = ""; cedula = ""; password = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar Empleado", fontWeight = FontWeight.Bold) }
        if (mensaje != null) Text(mensaje!!, color = GrowthPurplePrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
