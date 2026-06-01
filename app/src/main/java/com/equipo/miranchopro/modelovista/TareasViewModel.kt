package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Tarea
import com.equipo.miranchopro.data.repository.TareaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TareasViewModel(private val repository: TareaRepository) : ViewModel() {

    val listaTareas: StateFlow<List<Tarea>> = repository.obtenerTodas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.agregarTarea(tarea)
        }
    }

    fun editarTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.actualizarTarea(tarea)
        }
    }

    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.eliminarTarea(tarea)
        }
    }

    fun toggleTareaCompletada(tarea: Tarea) {
        viewModelScope.launch {
            repository.actualizarTarea(tarea.copy(estaHecha = !tarea.estaHecha))
        }
    }
}
