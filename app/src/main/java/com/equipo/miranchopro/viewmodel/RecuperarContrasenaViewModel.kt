package com.equipo.miranchopro.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import kotlinx.coroutines.launch

class RecuperarContrasenaViewModel(private val usuarioDao: UsuarioDao) : ViewModel() {
    var correo by mutableStateOf("")

    var mensajeUI by mutableStateOf<String?>(null)
        private set
    var correoEnviado by mutableStateOf(false)
        private set

    fun limpiarMensaje() { mensajeUI = null }

    /**
     * CU-03: Recuperar contraseña
     */
    fun enviarEnlaceRecuperacion(simularFalloServidor: Boolean) {
        if (correo.isBlank()) {
            mensajeUI = "Por favor, ingresa tu correo electrónico"
            return
        }

        // CP-03.3 (Excepción): Falla el servidor de correos
        if (simularFalloServidor) {
            mensajeUI = "Error: No se pudo enviar el email. Intenta más tarde."
            return
        }

        // Ejecutamos la búsqueda en segundo plano (Room)
        viewModelScope.launch {
            val usuario = usuarioDao.buscarPorCorreo(correo.trim())

            if (usuario == null) {
                // CP-03.2 (Alternativo): Correo no existe en la BD
                mensajeUI = "El correo no coincide con ninguna cuenta registrada."
            } else {
                // CP-03.1 (Normal): Correo válido, se envía enlace
                mensajeUI = "Se ha enviado un enlace de recuperación a tu correo."
                correoEnviado = true
            }
        }
    }
}