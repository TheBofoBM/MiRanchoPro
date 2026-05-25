package com.equipo.miranchopro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.data.model.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrabajadoresViewModel(private val usuarioDao: UsuarioDao) : ViewModel() {

    val listaTrabajadores: StateFlow<List<Usuario>> = usuarioDao.obtenerTrabajadores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarTrabajador(correo: String, contrasena: String) {
        viewModelScope.launch {
            val nuevoTrabajador = Usuario(
                correo = correo,
                contrasena = contrasena,
                rol = "TRABAJADOR"
            )
            usuarioDao.registrarUsuario(nuevoTrabajador)
        }
    }

    fun actualizarTrabajador(usuario: Usuario) {
        viewModelScope.launch {
            usuarioDao.actualizarUsuario(usuario)
        }
    }

    fun darDeBajaTrabajador(usuario: Usuario) {
        viewModelScope.launch {
            usuarioDao.eliminarUsuario(usuario)
        }
    }
    
    suspend fun existeUsuario(correo: String): Boolean {
        return usuarioDao.buscarPorCorreo(correo) != null
    }
}
