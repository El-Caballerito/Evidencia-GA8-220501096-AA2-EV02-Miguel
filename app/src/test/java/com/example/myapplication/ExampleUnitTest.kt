package com.example.myapplication

import com.example.myapplication.data.local.entity.EmpleadoEntity
import com.example.myapplication.data.local.entity.MarcacionEntity
import com.example.myapplication.domain.usecase.AuthValidatorUseCase
import com.example.myapplication.domain.usecase.IaAuditorUseCase
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    private val authValidator = AuthValidatorUseCase()
    private val iaAuditor = IaAuditorUseCase()

    @Test
    fun prueba01_ReglaPasswordsPorRol() {
        val (okEmp, _) = authValidator.validarReglaPassword("J3U4AN38932", "Juan Perez", "200002", "Empleado")
        assertTrue(okEmp)

        val (badAdmin, _) = authValidator.validarReglaPassword("D1IANA48348", "Diana Morales", "100001", "Supervisor")
        assertFalse(badAdmin)

        val (okAdmin, _) = authValidator.validarReglaPassword("D1IANA483AD48MIN", "Diana Morales", "100001", "Supervisor")
        assertTrue(okAdmin)
    }

    @Test
    fun prueba02_BloqueoPorTresIntentosFallidos() {
        val emp = EmpleadoEntity(
            id = 1, codigoId = "200002", cedula = "1020304050", nombre = "Juan Perez",
            passwordHash = authValidator.encriptarPassword("J3U4AN38932"), datoBiometrico = "ROSTRO_OK"
        )
        val (_, _, emp1) = authValidator.procesarLogin(emp, "CLAVE_ERRONEA", true)
        val (_, _, emp2) = authValidator.procesarLogin(emp1, "CLAVE_ERRONEA", true)
        val (ok3, msg3, emp3) = authValidator.procesarLogin(emp2, "CLAVE_ERRONEA", true)

        assertFalse(ok3)
        assertTrue(emp3!!.bloqueado)
        assertTrue(msg3.contains("bloqueada"))
    }

    @Test
    fun prueba03_MotorIaAuditoriaJustificaciones() {
        val (estadoAuto, _) = iaAuditor.auditarJustificacionIa(
            "Cita medica", "Asistencia a cita medica general en EPS Sura", "soporte_eps.pdf"
        )
        assertEquals("Aprobada IA", estadoAuto)

        val (estadoEscalado, _) = iaAuditor.auditarJustificacionIa(
            "Olvido de marcacion", "Olvide marcar entrada hoy", null
        )
        assertEquals("Pendiente", estadoEscalado)
    }

    @Test
    fun prueba04_CalculoJornadaColombiana7Horas() {
        val base = 1_700_000_000_000L
        val min = 60_000L
        val marcs = listOf(
            MarcacionEntity(empleadoId = 1, fecha = "2026-11-26", tipoEvento = "Entrada", horaRegistro = base),
            MarcacionEntity(empleadoId = 1, fecha = "2026-11-26", tipoEvento = "Inicio Almuerzo", horaRegistro = base + 240 * min),
            MarcacionEntity(empleadoId = 1, fecha = "2026-11-26", tipoEvento = "Fin Almuerzo", horaRegistro = base + 300 * min),
            MarcacionEntity(empleadoId = 1, fecha = "2026-11-26", tipoEvento = "Salida", horaRegistro = base + 510 * min)
        )
        val res = iaAuditor.calcularResumenJornada(marcs)
        assertEquals("7h 0m", res.normales)
        assertEquals("0h 30m", res.extras)
        assertEquals("0h 0m", res.faltantes)
    }
}
