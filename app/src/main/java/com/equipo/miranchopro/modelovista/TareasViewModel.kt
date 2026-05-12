package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.equipo.miranchopro.data.model.Prioridad
import com.equipo.miranchopro.data.model.Tarea

class TareasViewModel : ViewModel() {
    private val _listaTareas = mutableStateListOf(
        Tarea(1, "Revisión de cercas - Sector Norte", "Inspeccionar y reparar cercas en el sector norte", "Juan Pérez", Prioridad.ALTA, estaHecha = true),
        Tarea(2, "Limpieza de bebederos", "Limpiar y desinfectar todos los bebederos", "Carlos López", Prioridad.MEDIA),
        Tarea(3, "Vacunación - Lote A", "Aplicar vacuna triple a animales del Lote A", "María García", Prioridad.ALTA, fecha = "08/04/2026"),
        Tarea(4, "Inventario de alimento", "Revisar existencias de alimento y forraje", "Ana Martínez", Prioridad.MEDIA, fecha = "09/04/2026")
    )
    val listaTareas: List<Tarea> get() = _listaTareas

    fun agregarTarea(tarea: Tarea) {
        val nuevoId = (_listaTareas.maxOfOrNull { it.id } ?: 0) + 1
        _listaTareas.add(tarea.copy(id = nuevoId))
    }

    fun editarTarea(tarea: Tarea) {
        val index = _listaTareas.indexOfFirst { it.id == tarea.id }
        if (index != -1) {
            _listaTareas[index] = tarea
        }
    }

    fun eliminarTarea(tarea: Tarea) {
        _listaTareas.remove(tarea)
    }

    fun toggleTareaCompletada(tarea: Tarea) {
        val index = _listaTareas.indexOfFirst { it.id == tarea.id }
        if (index != -1) {
            _listaTareas[index] = tarea.copy(estaHecha = !tarea.estaHecha)
        }
    }
}
