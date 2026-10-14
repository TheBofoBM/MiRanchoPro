package com.equipo.miranchopro.domain.usecase

import android.util.Patterns
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.data.model.Usuario

sealed class ResultadoRegistro {
    object Exito : ResultadoRegistro()
    object CorreoDuplicado : ResultadoRegistro()
    object FormatoInvalido : ResultadoRegistro()
    data class Error(val mensaje: String) : ResultadoRegistro()
}

class RegistrarUsuarioUseCase(private val usuarioDao: UsuarioDao) {

    suspend fun ejecutar(correo: String, contrasena: String, confirmarContrasena: String): ResultadoRegistro {
        val correoNormalizado = com.equipo.miranchopro.utils.Validaciones.correo(correo)

        // 1. Validación de campos vacíos
        if (correo.isBlank() || contrasena.isBlank() || confirmarContrasena.isBlank()) {
            return ResultadoRegistro.Error("Por favor, llena todos los campos")
        }

        // 2. Validación de formato (Regex básica para el test, Patterns requiere Android)
        if (!com.equipo.miranchopro.utils.Validaciones.correoValido(correoNormalizado)) {
            return ResultadoRegistro.FormatoInvalido
        }

        // 3. Seguridad de contraseña
        if (contrasena.length < 6) {
            return ResultadoRegistro.Error("La contraseña debe tener al menos 6 caracteres")
        }

        // 4. Coincidencia
        if (contrasena != confirmarContrasena) {
            return ResultadoRegistro.Error("Las contraseñas no coinciden")
        }

        return try {
            val existe = usuarioDao.buscarPorCorreo(correoNormalizado)
            if (existe != null) {
                ResultadoRegistro.CorreoDuplicado
            } else {
                val nuevoUsuario = Usuario(
                    correo = correoNormalizado,
                    contrasena = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { com.equipo.miranchopro.utils.Claves.proteger(contrasena) },
                    rol = "Administrador"
                )
                usuarioDao.registrarSeguro(nuevoUsuario)
                ResultadoRegistro.Exito
            }
        } catch (e: Exception) {
            ResultadoRegistro.Error(com.equipo.miranchopro.utils.Validaciones.mensaje(e, "No se pudo crear la cuenta. Inténtalo de nuevo."))
        }
    }
}
