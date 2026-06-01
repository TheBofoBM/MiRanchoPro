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
        
    var estaCargando by mutableStateOf(false)
        private set

    fun limpiarMensaje() { mensajeError = null }

    fun iniciarSesion(simularErrorConexion: Boolean) {
        if (simularErrorConexion) {
            mensajeError = "Sin conexión al servidor"
            return
        }

        if (correo.isBlank() || contrasena.isBlank()) {
            mensajeError = "Por favor, llena todos los campos"
            return
        }

        estaCargando = true
        viewModelScope.launch {
            try {
                val usuario = usuarioDao.iniciarSesion(correo.trim(), contrasena)
                if (usuario != null) {
                    loginExitoso = true
                } else {
                    mensajeError = "Usuario o contraseña incorrectos"
                }
            } catch (e: Exception) {
                mensajeError = "Error al conectar con la base de datos"
            } finally {
                estaCargando = false
            }
        }
    }
}
