package com.example.myapplication.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.database.GrowthDatabase
import com.example.myapplication.data.local.entity.*
import com.example.myapplication.domain.usecase.AuthValidatorUseCase
import com.example.myapplication.domain.usecase.IaAuditorUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun GrowthAppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { GrowthDatabase.getDatabase(context) }
    val dao = remember { db.growthDao() }
    val authUseCase = remember { AuthValidatorUseCase() }
    val iaUseCase = remember { IaAuditorUseCase() }
    val scope = rememberCoroutineScope()

    var usuarioActual by remember { mutableStateOf<EmpleadoEntity?>(null) }
    var geocercaValida by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "geocerca") {
        composable("geocerca") {
            GeocercaScreen(onValidado = { geocercaValida = true; navController.navigate("login") })
        }

        composable("login") {
            LoginScreen(
                onLoginSubmit = { nombre, id, pwd, bioOk, onError ->
                    scope.launch {
                        val emp = dao.buscarEmpleadoLogin(nombre.trim(), id.trim())
                        val (ok, msg, empAct) = authUseCase.procesarLogin(emp, pwd, bioOk)
                        if (empAct != null) dao.actualizarEmpleado(empAct)
                        if (ok && empAct != null) {
                            usuarioActual = empAct
                            navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                        } else onError(msg)
                    }
                },
                onIrRecuperar = { navController.navigate("recuperar") }
            )
        }

        composable("recuperar") {
            RecuperarPasswordScreen(
                onEnviarSolicitud = { sol -> scope.launch { dao.insertarSolicitudPassword(sol) } },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("dashboard") {
            val emp = usuarioActual
            if (emp == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                var vistaActual by remember { mutableStateOf("inicio") }
                val marcaciones by dao.observarMarcaciones(emp.id).collectAsState(initial = emptyList())
                val turnos by dao.observarHorarios(emp.id).collectAsState(initial = emptyList())
                val justEmp by dao.observarJustificacionesEmpleado(emp.id).collectAsState(initial = emptyList())
                val pendientes by dao.observarJustificacionesPendientes().collectAsState(initial = emptyList())
                val empleadosSede by dao.observarEmpleadosPorSede(emp.sede).collectAsState(initial = emptyList())
                val resumen = remember(marcaciones) { iaUseCase.calcularResumenJornada(marcaciones) }

                GrowthMenuWrapper(
                    esAdmin = emp.esAdmin,
                    vistaActual = vistaActual,
                    onSeleccionarVista = { vistaActual = it },
                    onLogout = { usuarioActual = null; navController.navigate("login") { popUpTo(0) } }
                ) {
                    when (vistaActual) {
                        "historial" -> HistorialAsistenciaScreen(marcaciones) { vistaActual = "inicio" }
                        "resumen" -> ResumenHorasScreen(resumen, marcaciones) { vistaActual = "inicio" }
                        "configuracion" -> ConfiguracionPerfilScreen(
                            empleado = emp,
                            onActualizarBiometria = { nuevoTipo ->
                                val actualizado = emp.copy(tipoBiometria = nuevoTipo)
                                usuarioActual = actualizado
                                scope.launch { dao.actualizarEmpleado(actualizado) }
                            },
                            onVolver = { vistaActual = "inicio" }
                        )
                        "registro" -> RegistroEmpleadoScreen(
                            authUseCase = authUseCase,
                            onRegistrar = { nuevoEmp -> scope.launch { dao.insertarEmpleado(nuevoEmp) } },
                            onVolver = { vistaActual = "inicio" }
                        )
                        else -> DashboardPrincipalScreen(
                            empleado = emp, marcaciones = marcaciones, turnos = turnos,
                            justificacionesEmp = justEmp, pendientesAdmin = pendientes, empleadosSede = empleadosSede,
                            iaUseCase = iaUseCase,
                            onMarcar = { tipo ->
                                scope.launch {
                                    dao.insertarMarcacion(
                                        MarcacionEntity(
                                            empleadoId = emp.id, fecha = LocalDate.now().toString(),
                                            tipoEvento = tipo, horaRegistro = System.currentTimeMillis(), geocercaValida = geocercaValida
                                        )
                                    )
                                }
                            },
                            onCrearJustificacion = { tipo, desc, archivo ->
                                scope.launch {
                                    val (est, obs) = iaUseCase.auditarJustificacionIa(tipo, desc, archivo)
                                    dao.insertarJustificacion(
                                        JustificacionEntity(
                                            empleadoId = emp.id, tipoNovedad = tipo, fechaNovedad = LocalDate.now().toString(),
                                            descripcion = desc, archivoSoporte = archivo, estado = est,
                                            observacionRevision = obs, revisadoPor = if (est == "Aprobada IA") "Motor IA Local" else null
                                        )
                                    )
                                }
                            },
                            onDecidirJustificacion = { j, apr ->
                                scope.launch { dao.actualizarJustificacion(j.copy(estado = if (apr) "Aprobada" else "Rechazada", revisadoPor = emp.nombre)) }
                            },
                            onAuditarNomina = { onRes ->
                                scope.launch {
                                    val (resIa, det) = iaUseCase.auditarNominaLocal(empleadosSede, pendientes.size)
                                    dao.insertarAuditoriaNomina(
                                        AuditoriaNominaEntity(
                                            sede = emp.sede, periodo = LocalDate.now().toString().substring(0, 7),
                                            resultadoIa = resIa, detalleHallazgos = det, aprobadoHumano = (resIa == "Validado IA"), ejecutadoPor = emp.nombre
                                        )
                                    )
                                    onRes("$resIa: $det")
                                }
                            },
                            onLogout = { usuarioActual = null; navController.navigate("login") { popUpTo(0) } }
                        )
                    }
                }
            }
        }
    }
}
