package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "empleados",
    indices = [Index(value = ["codigoId"], unique = true), Index(value = ["cedula"], unique = true)]
)
data class EmpleadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigoId: String,
    val cedula: String,
    val nombre: String,
    val rol: String = "Empleado",
    val sede: String = "Antioquia-Medellín-El Poblado-Central",
    val area: String = "Operaciones",
    val estado: String = "Trabajando",
    val passwordHash: String,
    val intentosFallidos: Int = 0,
    val bloqueado: Boolean = false,
    val tipoBiometria: String = "Huella",
    val datoBiometrico: String,
    val fotoPerfil: String = "default_avatar.png"
) {
    val esAdmin: Boolean
        get() = rol in listOf("Supervisor", "Recursos Humanos", "Jefe de Sede")
}

@Entity(
    tableName = "marcaciones",
    foreignKeys = [ForeignKey(entity = EmpleadoEntity::class, parentColumns = ["id"], childColumns = ["empleadoId"], onDelete = ForeignKey.CASCADE)]
)
data class MarcacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val empleadoId: Int,
    val fecha: String,
    val tipoEvento: String,
    val horaRegistro: Long,
    val geocercaValida: Boolean = true
)

@Entity(
    tableName = "justificaciones",
    foreignKeys = [ForeignKey(entity = EmpleadoEntity::class, parentColumns = ["id"], childColumns = ["empleadoId"], onDelete = ForeignKey.CASCADE)]
)
data class JustificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val empleadoId: Int,
    val tipoNovedad: String,
    val fechaNovedad: String,
    val descripcion: String,
    val archivoSoporte: String? = null,
    val estado: String = "Pendiente",
    val observacionRevision: String? = null,
    val revisadoPor: String? = null
)

@Entity(tableName = "horarios_turnos")
data class HorarioTurnoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val empleadoId: Int,
    val diaSemana: String,
    val horaInicio: String?,
    val horaFin: String?,
    val esDescanso: Boolean = false
)

@Entity(tableName = "notificaciones")
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val empleadoId: Int,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis()
)

@Entity(tableName = "solicitudes_password")
data class SolicitudPasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreIngresado: String,
    val identificador: String,
    val nuevaPasswordSolicitada: String,
    val motivo: String,
    val estado: String = "Pendiente"
)

@Entity(tableName = "auditorias_nomina")
data class AuditoriaNominaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sede: String,
    val periodo: String,
    val resultadoIa: String,
    val detalleHallazgos: String,
    val aprobadoHumano: Boolean = false,
    val ejecutadoPor: String
)
