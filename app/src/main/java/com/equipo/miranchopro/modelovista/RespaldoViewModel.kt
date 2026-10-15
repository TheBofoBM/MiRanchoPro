package com.equipo.miranchopro.modelovista

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.local.*
import com.equipo.miranchopro.data.model.Usuario
import kotlinx.coroutines.*

class RespaldoViewModel(context: Context,private val db: RanchoDatabase,private val usuario: () -> Usuario?) : OperacionViewModel() {
    private val servicio=RespaldoRancho(context.applicationContext,db)
    var revision by mutableStateOf<RespaldoRancho.Revision?>(null)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var restaurado by mutableStateOf(false)
        private set
    fun exportar(uri: Uri,clave: String)=operar {
        require(usuario()?.rol?.equals("Administrador",true)==true) { "Solo el dueño puede crear respaldos" }
        servicio.exportar(uri,clave);mensaje="Respaldo guardado. Conserva su contraseña para recuperarlo."
    }
    fun revisar(uri: Uri,clave: String)=operar {
        autorizar();revision?.let { servicio.descartar(it) };revision=null
        revision=servicio.revisar(uri,clave)
    }
    fun restaurar(claveActual: String)=operar {
        autorizar()
        usuario()?.let { u -> require(db.usuarioDao().iniciarSesion(u.correo,claveActual)!=null) { "La contraseña actual no coincide" } }
        val respaldo=revision ?: throw IllegalArgumentException("Selecciona un respaldo primero")
        servicio.restaurar(respaldo);revision=null;restaurado=true
    }
    fun cancelar() { revision?.let { servicio.descartar(it) };revision=null }
    private suspend fun autorizar() {
        require(usuario()?.rol?.equals("Administrador",true)==true || (usuario()==null && db.usuarioDao().cantidad()==0)) { "Entra con la cuenta del dueño para reemplazar los registros existentes" }
    }
    override fun onCleared() { revision?.let { servicio.descartar(it) };super.onCleared() }
}
