package com.equipo.miranchopro.modelovista
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.*
import com.equipo.miranchopro.data.repository.TareaRepository
import com.equipo.miranchopro.data.local.dao.UsuarioDao
import com.equipo.miranchopro.utils.Validaciones
import kotlinx.coroutines.flow.*

class TareasViewModel(private val repository: TareaRepository, private val usuarios: UsuarioDao? = null, private val actual: () -> Usuario? = {null}) : OperacionViewModel() {
    val listaTareas=repository.obtenerTodas().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    private fun exigirDueno() { require(actual()?.rol?.equals("Administrador",true)==true) { "Solo el dueño puede administrar tareas" } }
    private suspend fun validar(t: Tarea) {
        exigirDueno()
        require(t.titulo.isNotBlank() && t.descripcion.isNotBlank()) { "Título y descripción son obligatorios" }
        require(usuarios?.buscarPorCorreo(t.responsable)!=null) { "Selecciona un responsable activo" }
        require(t.fecha.isNullOrBlank() || Validaciones.errorFecha(t.fecha,"La fecha",permitirFutura=true)==null) { "La fecha de la tarea no es válida" }
    }
    fun agregarTarea(tarea: Tarea)=operar { validar(tarea); repository.agregarTarea(tarea) }
    fun editarTarea(tarea: Tarea)=operar { validar(tarea); repository.actualizarTarea(tarea) }
    fun eliminarTarea(tarea: Tarea)=operar { exigirDueno(); repository.eliminarTarea(tarea) }
    fun toggleTareaCompletada(tarea: Tarea)=operar {
        val usuario=actual() ?: throw IllegalArgumentException("Inicia sesión")
        val actual=repository.porId(tarea.id) ?: throw IllegalArgumentException("La tarea ya no existe")
        require(usuario.rol.equals("Administrador",true) || (usuario.rol=="TRABAJADOR" && actual.responsable==usuario.correo)) { "Esta tarea no está asignada a tu cuenta" }
        repository.actualizarTarea(actual.copy(estaHecha=!actual.estaHecha))
    }
}
