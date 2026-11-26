package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthDao {
    // Empleados
    @Query("SELECT * FROM empleados WHERE LOWER(nombre) = LOWER(:nombre) AND (codigoId = :id OR cedula = :id) LIMIT 1")
    suspend fun buscarEmpleadoLogin(nombre: String, id: String): EmpleadoEntity?

    @Query("SELECT * FROM empleados WHERE sede = :sede")
    fun observarEmpleadosPorSede(sede: String): Flow<List<EmpleadoEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarEmpleado(empleado: EmpleadoEntity): Long

    @Update
    suspend fun actualizarEmpleado(empleado: EmpleadoEntity)

    @Query("SELECT COUNT(*) FROM empleados")
    suspend fun contarEmpleados(): Int

    // Marcaciones
    @Query("SELECT * FROM marcaciones WHERE empleadoId = :empId ORDER BY horaRegistro DESC")
    fun observarMarcaciones(empId: Int): Flow<List<MarcacionEntity>>

    @Insert
    suspend fun insertarMarcacion(marcacion: MarcacionEntity)

    // Justificaciones
    @Query("SELECT * FROM justificaciones WHERE empleadoId = :empId ORDER BY id DESC")
    fun observarJustificacionesEmpleado(empId: Int): Flow<List<JustificacionEntity>>

    @Query("SELECT * FROM justificaciones WHERE estado = 'Pendiente' ORDER BY id DESC")
    fun observarJustificacionesPendientes(): Flow<List<JustificacionEntity>>

    @Insert
    suspend fun insertarJustificacion(justificacion: JustificacionEntity)

    @Update
    suspend fun actualizarJustificacion(justificacion: JustificacionEntity)

    // Horarios, Notificaciones y Auditoría
    @Query("SELECT * FROM horarios_turnos WHERE empleadoId = :empId")
    fun observarHorarios(empId: Int): Flow<List<HorarioTurnoEntity>>

    @Insert
    suspend fun insertarHorarios(turnos: List<HorarioTurnoEntity>)

    @Query("SELECT * FROM notificaciones WHERE empleadoId = :empId ORDER BY creadoEn DESC LIMIT 10")
    fun observarNotificaciones(empId: Int): Flow<List<NotificacionEntity>>

    @Insert
    suspend fun insertarNotificacion(notificacion: NotificacionEntity)

    @Insert
    suspend fun insertarSolicitudPassword(solicitud: SolicitudPasswordEntity)

    @Insert
    suspend fun insertarAuditoriaNomina(auditoria: AuditoriaNominaEntity)
}
