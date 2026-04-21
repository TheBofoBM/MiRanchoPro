package com.equipo.miranchopro.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.data.model.Usuario
import kotlinx.coroutines.launch

class RegistroViewModel(private val usuarioDao: UsuarioDao) : ViewModel() {
    var correo by mutableStateOf("")
    var contrasena by mutableStateOf("")
    var confirmarContrasena by mutableStateOf("")

    var mensajeError by mutableStateOf<String?>(null)
        private set
    var registroExitoso by mutableStateOf(false)
        private set

    fun limpiarMensaje() { mensajeError = null }

    fun registrarUsuario(simularErrorBD: Boolean) {
        if (simularErrorBD) {
            mensajeError = "Error al crear cuenta. Por favor, reintenta." // CP-02.3
            return
        }

        if (correo.isBlank() || contrasena.isBlank() || confirmarContrasena.isBlank()) {
            mensajeError = "Por favor, llena todos los campos"
            return
        }

        if (contrasena != confirmarContrasena) {
            mensajeError = "Las contraseñas no coinciden"
            return
        }

        // Ejecutamos la consulta en segundo plano
        viewModelScope.launch {
            val usuarioExistente = usuarioDao.buscarPorCorreo(correo.trim())

            if (usuarioExistente != null) {
                // CP-02.2: El correo ya existe en la Base de Datos
                mensajeError = "El correo ya está registrado. Sugerencia: Inicia sesión."
            } else {
                // CP-02.1: Éxito. Guardamos en la BD real.
                val nuevoUsuario = Usuario(correo = correo.trim(), contrasena = contrasena, rol = "Administrador")
                usuarioDao.registrarUsuario(nuevoUsuario)
                registroExitoso = true
            }
        }
    }
}