package com.equipo.miranchopro.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class SesionViewModel(context: Context, private val usuarios: UsuarioDao) : ViewModel() {
    private val preferencias = context.applicationContext.getSharedPreferences("sesion", Context.MODE_PRIVATE)
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario = _usuario.asStateFlow()
    val cargando = MutableStateFlow(true)
    val error = MutableStateFlow<String?>(null)
    init { restaurar() }
    fun restaurar() {
        cargando.value = true
        error.value = null
        viewModelScope.launch {
            try { preferencias.getString("correo", null)?.let { _usuario.value = usuarios.buscarPorCorreo(it)?.copy(contrasena = "") } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = "No se pudieron abrir los datos. Se conservaron tus registros. Inténtalo de nuevo." }
            finally { cargando.value = false }
        }
    }
    fun entrar(usuario: Usuario) {
        _usuario.value = usuario.copy(contrasena = "")
        preferencias.edit().putString("correo", usuario.correo).apply()
    }
    fun salir() { preferencias.edit().clear().apply(); _usuario.value = null }
}
