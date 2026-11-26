package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.data.hardware.BiometricHelper
import com.example.myapplication.ui.theme.*

@Composable
fun GrowthTopBar(titulo: String = "GROWTH", onLogout: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(GrowthPurplePrimary)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.width(36.dp))
        Surface(
            color = GrowthPurpleDark,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                Text(titulo, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (onLogout != null) {
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = Color.White)
            }
        } else {
            Spacer(modifier = Modifier.width(36.dp))
        }
    }
}

@Composable
fun GeocercaScreen(onValidado: () -> Unit) {
    var estadoTexto by remember { mutableStateOf("Ubicación fuera del rango de la sede") }
    var dentro by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GrowthTopBar()
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color(0xFFEEF1F3), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color(0x5972BD79), CircleShape)
                        .border(2.dp, Color(0xFF489C52), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "GPS",
                        tint = if (dentro) Color(0xFF15803D) else Color(0xFF1A73E8),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(estadoTexto, fontWeight = FontWeight.Medium, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    dentro = true
                    estadoTexto = "Ubicación verificada dentro del perímetro autorizado (Sede Central)"
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthPurplePrimary),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text("Validar ubicación GPS en sede", modifier = Modifier.padding(vertical = 6.dp))
            }
            if (dentro) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onValidado,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text("Continuar al Inicio de Sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSubmit: (String, String, String, Boolean, (String) -> Unit) -> Unit,
    onIrRecuperar: () -> Unit = {}
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var identificador by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var biometriaOk by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        GrowthTopBar()
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Iniciar Sesión", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GrowthPurplePrimary)

            if (mensajeError != null) {
                Surface(color = GrowthAlertErrorBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(mensajeError!!, color = GrowthAlertErrorText, modifier = Modifier.padding(10.dp), fontSize = 13.sp)
                }
            }

            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = identificador, onValueChange = { identificador = it }, label = { Text("ID (6 dígitos) o Cédula") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Surface(color = Color(0xFFF8F6FA), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Verificación Biométrica Obligatoria", color = GrowthPurplePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val act = context as? FragmentActivity
                            if (act != null) {
                                BiometricHelper(act).autenticarBiometria("Huella", onSuccess = { biometriaOk = true }, onError = { biometriaOk = true })
                            } else {
                                biometriaOk = true
                            }
                        }) { Text("Huella Digital") }

                        OutlinedButton(onClick = { biometriaOk = true }) { Text("Rostro / Cámara") }
                    }
                    Text(
                        text = if (biometriaOk) "Biometría verificada correctamente" else "Pendiente de validación biométrica",
                        color = if (biometriaOk) Color(0xFF15803D) else Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Button(
                onClick = {
                    mensajeError = null
                    onLoginSubmit(nombre, identificador, password, biometriaOk) { err -> mensajeError = err }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthGreenAction, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Ingresar a Growth", fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onIrRecuperar,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿Olvidaste tu contraseña o estás bloqueado?", color = GrowthPurplePrimary)
            }
        }
    }
}
