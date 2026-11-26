package com.example.myapplication.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.GrowthDao
import com.example.myapplication.data.local.entity.*
import com.example.myapplication.domain.usecase.AuthValidatorUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EmpleadoEntity::class, MarcacionEntity::class, JustificacionEntity::class,
        HorarioTurnoEntity::class, NotificacionEntity::class,
        SolicitudPasswordEntity::class, AuditoriaNominaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GrowthDatabase : RoomDatabase() {
    abstract fun growthDao(): GrowthDao

    companion object {
        @Volatile
        private var INSTANCE: GrowthDatabase? = null

        fun getDatabase(context: Context): GrowthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrowthDatabase::class.java,
                    "growth_dev.db"
                )
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.growthDao()
                    val auth = AuthValidatorUseCase()
                    val adminId = dao.insertarEmpleado(
                        EmpleadoEntity(
                            codigoId = "100001", cedula = "1035432100", nombre = "Diana Morales",
                            rol = "Supervisor", passwordHash = auth.encriptarPassword("D1IANA483AD48MIN"),
                            tipoBiometria = "Huella", datoBiometrico = "HUELLA_SUPERVISOR_OK"
                        )
                    ).toInt()
                    val empId = dao.insertarEmpleado(
                        EmpleadoEntity(
                            codigoId = "200002", cedula = "1020304050", nombre = "Juan Perez",
                            rol = "Empleado", passwordHash = auth.encriptarPassword("J3U4AN38932"),
                            tipoBiometria = "Camara", datoBiometrico = "ROSTRO_EMPLEADO_OK"
                        )
                    ).toInt()

                    listOf(adminId, empId).forEach { id ->
                        val turnos = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").map { dia ->
                            val dom = dia == "Domingo"
                            HorarioTurnoEntity(
                                empleadoId = id, diaSemana = dia,
                                horaInicio = if (dom) null else "08:00",
                                horaFin = if (dom) null else "16:00",
                                esDescanso = dom
                            )
                        }
                        dao.insertarHorarios(turnos)
                    }
                }
            }
        }
    }
}
