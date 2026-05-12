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
    DETALLE_CATEGORIA,
    DADOS_DE_BAJA
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

    // Secciones fijas del rancho
    val tiposFijos = listOf("Vaca", "Toro", "Becerro", "Novillo", "Vaquilla")

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

    // Categorías fijas con su conteo actual
    val categorias: List<Pair<String, Int>>
        get() {
            val mapaConteo = listaAnimales
                .filter { it.estado != "Baja" }
                .groupBy { it.tipo }
                .mapValues { it.value.size }
            
            return tiposFijos.map { tipo ->
                tipo to (mapaConteo[tipo] ?: 0)
            }
        }

    // Lista de animales que SI están de baja
    val animalesDeBaja: List<Animal>
        get() = listaAnimales.filter { it.estado == "Baja" }

    val animalesFiltrados: List<Animal>
        get() {
            val base = if (vistaActual == VistaInventario.DADOS_DE_BAJA) {
                animalesDeBaja
            } else if (categoriaSeleccionada != null) {
                listaAnimales.filter { it.tipo == categoriaSeleccionada && it.estado != "Baja" }
            } else {
                listaAnimales.filter { it.estado != "Baja" }
            }
            
            return if (busqueda.isBlank()) {
                base
            } else {
                base.filter {
                    it.idArete.contains(busqueda, ignoreCase = true) ||
                    (it.nombre ?: "").contains(busqueda, ignoreCase = true) ||
                    it.raza.contains(busqueda, ignoreCase = true)
                }
            }
        }

    fun seleccionarCategoria(categoria: String) {
        categoriaSeleccionada = categoria
        vistaActual = VistaInventario.DETALLE_CATEGORIA
    }

    fun verBajas() {
        vistaActual = VistaInventario.DADOS_DE_BAJA
        categoriaSeleccionada = null
    }

    fun volverACategorias() {
        categoriaSeleccionada = null
        vistaActual = VistaInventario.CATEGORIAS
        busqueda = ""
    }
}
