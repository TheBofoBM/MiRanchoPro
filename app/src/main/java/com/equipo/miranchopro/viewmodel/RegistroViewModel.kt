package com.equipo.miranchopro.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.domain.usecase.RegistrarUsuarioUseCase
import com.equipo.miranchopro.domain.usecase.ResultadoRegistro
import kotlinx.coroutines.launch

class RegistroViewModel(private val usuarioDao: UsuarioDao) : ViewModel() {
    var correo by mutableStateOf("")
    var contrasena by mutableStateOf("")
    var confirmarContrasena by mutableStateOf("")

    var mensajeError by mutableStateOf<String?>(null)
        private set
    var registroExitoso by mutableStateOf(false)
        private set

    private val registrarUsuarioUseCase = RegistrarUsuarioUseCase(usuarioDao)

    fun limpiarMensaje() { mensajeError = null }

    fun registrarUsuario(simularErrorBD: Boolean) {
        if (simularErrorBD) {
            mensajeError = "Error al crear cuenta. Por favor, reintenta."
            return
        }

        viewModelScope.launch {
            val resultado = registrarUsuarioUseCase.ejecutar(
                correo = correo,
                contrasena = contrasena,
                confirmarContrasena = confirmarContrasena
            )

            when (resultado) {
                is ResultadoRegistro.Exito -> {
                    registroExitoso = true
                }
                is ResultadoRegistro.CorreoDuplicado -> {
                    mensajeError = "El correo ya está registrado. Sugerencia: Inicia sesión."
                }
                is ResultadoRegistro.FormatoInvalido -> {
                    mensajeError = "El formato del correo no es válido."
                }
                is ResultadoRegistro.Error -> {
                    mensajeError = resultado.mensaje
                }
            }
        }
    }
}
