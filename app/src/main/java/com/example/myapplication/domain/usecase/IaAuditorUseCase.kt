package com.example.myapplication.domain.usecase

import com.example.myapplication.data.local.entity.EmpleadoEntity
import com.example.myapplication.data.local.entity.MarcacionEntity

data class ResumenJornada(
    val normales: String,
    val extras: String,
    val faltantes: String,
    val minutosTrabajados: Int
)

class IaAuditorUseCase {

    private val palabrasMedicas = setOf(
        "cita", "medico", "médico", "doctor", "incapacidad", "eps",
        "urgencias", "consulta", "formula", "fórmula", "examen", "salud"
    )

    fun auditarJustificacionIa(
        tipoNovedad: String,
        descripcion: String,
        archivoSoporte: String?
    ): Pair<String, String> {
        val tiposAuto = setOf("Cita medica", "Incapacidad medica", "Cita médica", "Incapacidad médica")
        val descLimpia = descripcion.trim().lowercase()

        if (tipoNovedad !in tiposAuto) {
            return "Pendiente" to "Escalado a revisión humana: '$tipoNovedad' requiere validación del supervisor."
        }
        if (archivoSoporte.isNullOrBlank()) {
            return "Pendiente" to "Escalado a revisión humana: no se adjuntó archivo de soporte médico."
        }
        val ext = archivoSoporte.substringAfterLast('.', "").lowercase()
        if (ext !in setOf("pdf", "jpg", "jpeg", "png")) {
            return "Pendiente" to "Escalado a revisión humana: formato de soporte no reconocido por el auditor IA."
        }
        val coherente = descLimpia.length >= 12 && palabrasMedicas.any { it in descLimpia }
        if (!coherente) {
            return "Pendiente" to "Escalado a revisión humana: descripción breve o sin contexto médico coherente."
        }
        return "Aprobada IA" to "Aprobada automáticamente por Motor IA Local: soporte médico válido y descripción coherente. Turno ajustado."
    }

    fun calcularResumenJornada(marcaciones: List<MarcacionEntity>): ResumenJornada {
        if (marcaciones.isEmpty()) return ResumenJornada("0h 0m", "0h 0m", "0h 0m", 0)

        var totalNormales = 0
        var totalExtras = 0
        var totalFaltantes = 0

        marcaciones.groupBy { it.fecha }.values.forEach { eventosDia ->
            val ordenados = eventosDia.sortedBy { it.horaRegistro }
            val entrada = ordenados.firstOrNull { it.tipoEvento == "Entrada" }?.horaRegistro
            val salida = ordenados.firstOrNull { it.tipoEvento == "Salida" }?.horaRegistro
            val iniAlm = ordenados.firstOrNull { it.tipoEvento == "Inicio Almuerzo" }?.horaRegistro
            val finAlm = ordenados.firstOrNull { it.tipoEvento == "Fin Almuerzo" }?.horaRegistro

            if (entrada != null && salida != null && salida > entrada) {
                val minBrutos = ((salida - entrada) / 60000L).toInt()
                val minAlmuerzo = if (iniAlm != null && finAlm != null && finAlm > iniAlm) {
                    ((finAlm - iniAlm) / 60000L).toInt()
                } else 0

                val efectivos = maxOf(0, minBrutos - minAlmuerzo)
                if (efectivos >= 420) {
                    totalNormales += 420
                    totalExtras += (efectivos - 420)
                } else {
                    totalNormales += efectivos
                    totalFaltantes += (420 - efectivos)
                }
            }
        }

        fun fmt(mins: Int) = "${mins / 60}h ${mins % 60}m"
        return ResumenJornada(fmt(totalNormales), fmt(totalExtras), fmt(totalFaltantes), totalNormales + totalExtras)
    }

    fun auditarNominaLocal(empleadosSede: List<EmpleadoEntity>, pendientes: Int): Pair<String, String> {
        if (empleadosSede.isEmpty()) {
            return "Requiere Revision Humana" to "Fallo técnico: no se encontraron empleados activos en la sede."
        }
        val sinBio = empleadosSede.filter { it.datoBiometrico.isBlank() }
        if (sinBio.isNotEmpty() || pendientes > 0) {
            return "Requiere Revision Humana" to "Inconsistencias detectadas ($pendientes novedades pendientes y ${sinBio.size} registros biométricos incompletos)."
        }
        return "Validado IA" to "Auditoría local exitosa para ${empleadosSede.size} colaboradores. Sin novedades pendientes."
    }
}
