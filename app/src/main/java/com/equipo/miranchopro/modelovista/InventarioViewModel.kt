package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val repositorio: AnimalRepository = AnimalRepository()
) : ViewModel() {

    var listaAnimales by mutableStateOf<List<Animal>>(emptyList())
        private set

    var busqueda by mutableStateOf("")
    
    var estaCargando by mutableStateOf(false)
        private set

    init {
        cargarAnimales()
    }

    fun cargarAnimales() {
        viewModelScope.launch {
            estaCargando = true
            listaAnimales = repositorio.obtenerTodos()
            estaCargando = false
        }
    }

    val animalesFiltrados: List<Animal>
        get() = if (busqueda.isBlank()) {
            listaAnimales
        } else {
            listaAnimales.filter {
                it.idArete.contains(busqueda, ignoreCase = true) ||
                it.tipo.contains(busqueda, ignoreCase = true) ||
                it.raza.contains(busqueda, ignoreCase = true)
            }
        }
}
