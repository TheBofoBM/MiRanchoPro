package com.equipo.miranchopro.viewmodel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.modelovista.OperacionViewModel
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.data.model.Usuario
import com.equipo.miranchopro.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TrabajadoresViewModel(private val usuarioDao: UsuarioDao, private val puedeAdministrar: () -> Boolean = { false }, private val correoDueno: () -> String = { "" }) : OperacionViewModel() {
    val listaTrabajadores = usuarioDao.obtenerTrabajadores().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun agregarTrabajador(correo: String, contrasena: String) = operar {
        require(puedeAdministrar()) { "Solo el dueño puede administrar empleados" }
        require(Validaciones.correoValido(correo)) { "Ingresa un correo válido" }
        require(contrasena.length>=6) { "Usa una contraseña de al menos 6 caracteres" }
        val hash=withContext(Dispatchers.Default) { Claves.proteger(contrasena) }
        usuarioDao.registrarSeguro(Usuario(Validaciones.correo(correo),hash,"TRABAJADOR"))
    }
    fun actualizarTrabajador(usuario: Usuario) = operar {
        require(puedeAdministrar()) { "Solo el dueño puede administrar empleados" }
        val existente=usuarioDao.buscarPorCorreo(usuario.correo) ?: throw IllegalArgumentException("El empleado ya no existe")
        require(existente.rol=="TRABAJADOR") { "Esta cuenta no es un empleado" }
        require(usuario.contrasena.isEmpty() || usuario.contrasena.length>=6) { "Usa al menos 6 caracteres" }
        val hash=if(usuario.contrasena.isEmpty()) existente.contrasena else withContext(Dispatchers.Default) { Claves.proteger(usuario.contrasena) }
        usuarioDao.actualizarUsuario(existente.copy(contrasena=hash))
    }
    fun darDeBajaTrabajador(usuario: Usuario) = operar {
        require(puedeAdministrar() && usuario.rol=="TRABAJADOR") { "No tienes permiso para eliminar esta cuenta" }
        usuarioDao.eliminarEmpleado(usuario,correoDueno())
    }
    suspend fun existeUsuario(correo: String) = usuarioDao.buscarPorCorreo(Validaciones.correo(correo))!=null
}
