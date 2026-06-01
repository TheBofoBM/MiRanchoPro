package com.equipo.miranchopro.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class PasoRecuperacion {
    INGRESAR_CORREO,
    VERIFICAR_CODIGO,
    NUEVA_CONTRASENA,
    EXITO
}

class RecuperarContrasenaViewModel(private val usuarioDao: UsuarioDao) : ViewModel() {
    var correo by mutableStateOf("")
    var codigoIngresado by mutableStateOf("")
    var nuevaContrasena by mutableStateOf("")
    var confirmarContrasena by mutableStateOf("")

    var pasoActual by mutableStateOf(PasoRecuperacion.INGRESAR_CORREO)
    var mensajeUI by mutableStateOf<String?>(null)
        private set
    
    private var codigoGenerado by mutableStateOf("")

    fun limpiarMensaje() { mensajeUI = null }

    // Paso 1: Validar correo y enviar código
    fun enviarCodigo(simularFallo: Boolean) {
        if (correo.isBlank()) {
            mensajeUI = "Ingresa tu correo"
            return
        }
        if (simularFallo) {
            mensajeUI = "Ex-01: El servicio de correo no responde."
            return
        }

        viewModelScope.launch {
            val usuario = usuarioDao.buscarPorCorreo(correo.trim().lowercase())
            if (usuario == null) {
                mensajeUI = "El correo no está registrado."
            } else {
                // Generar código de 6 dígitos
                codigoGenerado = (100000..999999).random().toString()
                // Simulamos el envío (en una app real iría al email)
                mensajeUI = "Código enviado. (Para prueba usa: $codigoGenerado)"
                pasoActual = PasoRecuperacion.VERIFICAR_CODIGO
            }
        }
    }

    // Paso 2: Validar código (FA-01)
    fun verificarCodigo() {
        if (codigoIngresado == codigoGenerado) {
            pasoActual = PasoRecuperacion.NUEVA_CONTRASENA
        } else {
            mensajeUI = "FA-01: Código incorrecto. Intenta de nuevo."
        }
    }

    // Paso 3: Guardar nueva contraseña
    fun restablecerContrasena() {
        if (nuevaContrasena.length < 6) {
            mensajeUI = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        if (nuevaContrasena != confirmarContrasena) {
            mensajeUI = "Las contraseñas no coinciden"
            return
        }

        viewModelScope.launch {
            val usuario = usuarioDao.buscarPorCorreo(correo.trim().lowercase())
            if (usuario != null) {
                val usuarioActualizado = usuario.copy(contrasena = nuevaContrasena)
                usuarioDao.actualizarUsuario(usuarioActualizado)
                pasoActual = PasoRecuperacion.EXITO
                mensajeUI = "Contraseña actualizada con éxito"
            }
        }
    }
}
