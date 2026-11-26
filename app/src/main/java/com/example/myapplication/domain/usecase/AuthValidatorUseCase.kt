package com.example.myapplication.domain.usecase

import com.example.myapplication.data.local.entity.EmpleadoEntity
import java.security.MessageDigest
import kotlin.random.Random

class AuthValidatorUseCase {

    companion object {
        val ROLES_ADMIN = setOf("Supervisor", "Recursos Humanos", "Jefe de Sede")
    }

    fun generarCodigoId(cedula: String): String {
        val digitos = cedula.filter { it.isDigit() }
        val base = if (digitos.length >= 4) digitos.takeLast(4) else digitos.padStart(4, '0')
        val sufijo = Random.nextInt(10, 99).toString()
        return (base + sufijo).take(6)
    }

    fun validarReglaPassword(password: String, nombre: String, identificador: String, rol: String): Pair<Boolean, String> {
        if (password.length < 6) {
            return false to "La contraseña debe tener al menos 6 caracteres."
        }
        val pwdUpper = password.uppercase()
        val letrasNombre = nombre.uppercase().filter { it.isLetter() }.toSet()
        val numerosId = identificador.filter { it.isDigit() }.toSet()

        val tieneLetraNombre = pwdUpper.any { it in letrasNombre }
        val tieneNumId = pwdUpper.any { it in numerosId }

        if (!(tieneLetraNombre && tieneNumId)) {
            return false to "La contraseña debe mezclar letras de tu Nombre y números de tu ID o Cédula."
        }

        if (rol in ROLES_ADMIN) {
            for (letra in "ADMIN") {
                if (pwdUpper.count { it == letra } < "ADMIN".count { it == letra }) {
                    return false to "Para roles administrativos la contraseña debe incluir las letras de ADMIN."
                }
            }
        }
        return true to "Contraseña válida."
    }

    fun encriptarPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun procesarLogin(
        empleado: EmpleadoEntity?,
        passwordIngresada: String,
        biometriaOk: Boolean
    ): Triple<Boolean, String, EmpleadoEntity?> {
        if (empleado == null) return Triple(false, "Ingreso de datos incorrectos", null)
        if (!biometriaOk) return Triple(false, "Debe completar la verificación biométrica (Huella o Cámara).", empleado)
        if (empleado.bloqueado || empleado.intentosFallidos >= 3) {
            return Triple(false, "Usuario bloqueado por superar los 3 intentos. Solicite recuperación a RR.HH.", empleado)
        }

        val hashIngresado = encriptarPassword(passwordIngresada)
        if (empleado.passwordHash != hashIngresado) {
            val nuevosIntentos = empleado.intentosFallidos + 1
            val restantes = 3 - nuevosIntentos
            val actualizado = empleado.copy(
                intentosFallidos = nuevosIntentos,
                bloqueado = restantes <= 0
            )
            val msg = if (restantes <= 0) {
                "Contraseña incorrecta. Ha agotado sus 3 intentos; cuenta bloqueada."
            } else {
                "Contraseña incorrecta, tiene $restantes intentos más"
            }
            return Triple(false, msg, actualizado)
        }

        return Triple(true, "Bienvenido a Growth", empleado.copy(intentosFallidos = 0))
    }
}
