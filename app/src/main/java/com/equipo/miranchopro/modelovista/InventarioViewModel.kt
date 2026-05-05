package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.repository.AnimalRepository
import kotlinx.coroutines.launch

enum class VistaInventario {
    CATEGORIAS,
    DETALLE_CATEGORIA
}

class InventarioViewModel(
    private val repositorio: AnimalRepository
) : ViewModel() {

    var listaAnimales by mutableStateOf<List<Animal>>(emptyList())
        private set

    var busqueda by mutableStateOf("")
    
    var estaCargando by mutableStateOf(false)
        private set

    var vistaActual by mutableStateOf(VistaInventario.CATEGORIAS)
    var categoriaSeleccionada by mutableStateOf<String?>(null)

    init {
        observarAnimales()
    }

    private fun observarAnimales() {
        viewModelScope.launch {
            estaCargando = true
            repositorio.obtenerTodos().collect { lista ->
                listaAnimales = lista
                estaCargando = false
            }
        }
    }

    val categorias: List<Pair<String, Int>>
        get() = listaAnimales.groupBy { it.tipo }
            .map { it.key to it.value.size }
            .sortedBy { it.first }

    val animalesFiltrados: List<Animal>
        get() {
            val base = if (categoriaSeleccionada != null) {
                listaAnimales.filter { it.tipo == categoriaSeleccionada }
            } else {
                listaAnimales
            }
            
            return if (busqueda.isBlank()) {
                base
            } else {
                base.filter {
                    it.idArete.contains(busqueda, ignoreCase = true) ||
                    it.raza.contains(busqueda, ignoreCase = true)
                }
            }
        }

    fun seleccionarCategoria(categoria: String) {
        categoriaSeleccionada = categoria
        vistaActual = VistaInventario.DETALLE_CATEGORIA
    }

    fun volverACategorias() {
        categoriaSeleccionada = null
        vistaActual = VistaInventario.CATEGORIAS
        busqueda = ""
    }
}
