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

    fun guardarNuevoLote(lote: Lote) {
        viewModelScope.launch {
            repository.insertarLote(lote)
        }
    }

    // CU-14: Mover ganado de un lote a otro
    fun moverGanado(loteOrigen: Lote, loteDestino: Lote) {
        viewModelScope.launch {
            val cantidadAMover = loteOrigen.ocupacionActual

            // Actualizamos la capacidad de ambos lotes
            val loteOrigenActualizado = loteOrigen.copy(ocupacionActual = 0)
            val loteDestinoActualizado = loteDestino.copy(ocupacionActual = loteDestino.ocupacionActual + cantidadAMover)

            repository.actualizarLote(loteOrigenActualizado)
            repository.actualizarLote(loteDestinoActualizado)

            // NOTA: Aquí también deberías llamar a tu AnimalRepository para actualizar
            // el 'idLote' de los animales correspondientes para que se refleje en la base de datos.
        }
    }

    // Editar lote
    fun actualizarLote(lote: Lote) {
        viewModelScope.launch {
            repository.actualizarLote(lote)
        }
    }
}