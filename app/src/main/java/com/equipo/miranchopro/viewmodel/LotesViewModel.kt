package com.equipo.miranchopro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.miranchopro.data.model.Lote
import com.equipo.miranchopro.data.repository.LoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LotesViewModel(private val repository: LoteRepository) : ViewModel() {

    // Convertimos el Flow del DAO a un StateFlow para que la UI lo observe reactivamente
    val lotes: StateFlow<List<Lote>> = repository.obtenerTodosLosLotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // CU-16: Borrar lote validando ocupación
    fun eliminarLote(lote: Lote, onResultado: (Boolean, String) -> Unit) {
        if (lote.ocupacionActual == 0) {
            viewModelScope.launch {
                repository.eliminarLote(lote)
                onResultado(true, "Lote eliminado correctamente")
            }
        } else {
            onResultado(false, "No se puede eliminar: El lote aún tiene animales asignados.")
        }
    }

    // CU-14: Lógica auxiliar para validar el movimiento de ganado
    fun validarTraslado(loteDestino: Lote, cantidadAMover: Int): Boolean {
        return (loteDestino.ocupacionActual + cantidadAMover) <= loteDestino.capacidadMaxima
    }
}