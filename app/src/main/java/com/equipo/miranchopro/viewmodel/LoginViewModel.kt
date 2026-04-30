package com.equipo.miranchopro.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import kotlinx.coroutines.launch

class LoginViewModel(private val usuarioDao: UsuarioDao) : ViewModel() {
    var correo by mutableStateOf("")
    var contrasena by mutableStateOf("")

    var mensajeError by mutableStateOf<String?>(null)
        private set
    var loginExitoso by mutableStateOf(false)
        private set

    fun limpiarMensaje() { mensajeError = null }

    fun iniciarSesion(simularErrorConexion: Boolean) {
        if (simularErrorConexion) {
            mensajeError = "Sin conexión al servidor" // CP-01.3
            return
        }

        if (correo.isBlank() || contrasena.isBlank()) {
            mensajeError = "Por favor, llena todos los campos"
            return
        }

        // Consultamos la BD en segundo plano
        viewModelScope.launch {
            val usuario = usuarioDao.iniciarSesion(correo.trim(), contrasena)

            if (usuario != null) {
                loginExitoso = true // CP-01.1
            } else {
                mensajeError = "Usuario o contraseña incorrectos" // CP-01.2
            }
        }
    }
}