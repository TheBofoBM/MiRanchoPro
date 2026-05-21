package com.equipo.miranchopro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.data.repository.AnimalRepository
import com.equipo.miranchopro.data.repository.LoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LotesViewModel(
    private val loteRepo: LoteRepository,
    private val animalRepo: AnimalRepository // Agregamos el repositorio de animales
) : ViewModel() {

    // Combinamos los lotes con los animales en tiempo real para calcular la ocupación exacta
    val lotes: StateFlow<List<Lote>> = loteRepo.obtenerTodosLosLotes()
        .combine(animalRepo.obtenerTodos()) { listaLotes, listaAnimales ->
            listaLotes.map { lote ->
                val ocupacionReal = listaAnimales.count {
                    it.ubicacion == lote.nombre && it.estado != "Baja"
                }
                lote.copy(ocupacionActual = ocupacionReal)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertarLote(lote: Lote) {
        viewModelScope.launch {
            loteRepo.insertarLote(lote)
        }
    }

    fun actualizarLote(lote: Lote) {
        viewModelScope.launch {
            loteRepo.actualizarLote(lote)
        }
    }

    fun eliminarLote(lote: Lote, alTerminar: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                loteRepo.eliminarLote(lote)
                alTerminar(true, "Lote eliminado")
            } catch (e: Exception) {
                alTerminar(false, e.message ?: "Error")
            }
        }
    }

    fun moverGanado(loteOrigen: Lote, loteDestino: Lote) {
        // La función queda vacía ya que el traslado real,
        // validación de espacios y selección por casillas
        // ahora lo maneja exclusivamente DetalleLoteViewModel.
    }
}