package com.equipo.miranchopro.modelovista

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Animal
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.LoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetalleLoteViewModel(
    private val animalRepo: AnimalRepository,
    private val loteRepo: LoteRepository
) : ViewModel() {

    var loteActual by mutableStateOf<Lote?>(null)
        private set

    private val _animalesEnLote = MutableStateFlow<List<Animal>>(emptyList())
    val animalesEnLote: StateFlow<List<Animal>> = _animalesEnLote

    fun cargarDatosLote(nombreLote: String) {
        viewModelScope.launch {
            loteActual = loteRepo.buscarLotePorNombre(nombreLote)
            animalRepo.obtenerAnimalesPorLote(nombreLote).collect { lista ->
                _animalesEnLote.value = lista
            }
        }
    }

    // Funciones requeridas por la vista
    fun enviarAnimales(loteDestino: Lote, seleccionados: List<Animal>, alTerminar: () -> Unit) {
        viewModelScope.launch {
            seleccionados.forEach { animal ->
                animalRepo.updateAnimal(animal.copy(ubicacion = loteDestino.nombre))
            }
            loteActual?.let {
                val nuevoOrigen = it.copy(ocupacionActual = it.ocupacionActual - seleccionados.size)
                loteRepo.actualizarLote(nuevoOrigen)
                loteActual = nuevoOrigen
            }
            val nuevoDestino = loteDestino.copy(ocupacionActual = loteDestino.ocupacionActual + seleccionados.size)
            loteRepo.actualizarLote(nuevoDestino)
            alTerminar()
        }
    }

    fun actualizarLote(lote: Lote) {
        viewModelScope.launch {
            loteRepo.actualizarLote(lote)
            loteActual = lote
        }
    }

    fun eliminarLote(lote: Lote, alTerminar: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            loteRepo.eliminarLote(lote)
            alTerminar(true, "Eliminado")
        }
    }
}